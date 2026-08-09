package org.wx.core.wxBusiness.game.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PlayerTowerRun;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.enums.StageModeCode;
import org.wx.core.wxBusiness.game.entity.enums.TowerRunStatus;
import org.wx.core.wxBusiness.game.entity.vo.TowerRunVo;
import org.wx.core.wxBusiness.game.mapper.PlayerTowerRunMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TowerRunService extends WxServiceImpl<PlayerTowerRunMapper, PlayerTowerRun> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private StageProgressService stageProgressService;
    @Resource
    private StageService stageService;

    public PlayerTowerRun getByUid(String uid) {
        if (Wx.isEmpty(uid)) {
            return null;
        }
        return this.find().eq(PlayerTowerRun::getUid, uid).one();
    }

    public TowerRunVo status(String uid) {
        Stage root = stageProgressService.requireModeRoot(StageModeCode.TOWER);
        List<Stage> ordered = stageProgressService.listOrderedLevels(root.getId(), true);
        ErrorFactory.throwError(ordered.isEmpty(), "无尽塔暂无关卡");

        TowerRunVo vo = new TowerRunVo();
        vo.setStartLevelId(ordered.get(0).getId());
        PlayerTowerRun run = getByUid(uid);
        if (run != null && run.getStatus() == TowerRunStatus.RUNNING && !Wx.isEmpty(run.getCurrentLevelId())) {
            vo.setHasRunning(true);
            vo.setStatus(TowerRunStatus.RUNNING);
            fillLevelMeta(vo, run.getCurrentLevelId());
        } else {
            vo.setHasRunning(false);
            vo.setStatus(run != null ? run.getStatus() : null);
            fillLevelMeta(vo, ordered.get(0).getId());
            vo.setCurrentLevelId(null);
        }
        return vo;
    }

    /**
     * 进入无尽塔：始终从第一关重新开始（覆盖旧 run）。
     */
    @Transactional(rollbackFor = Exception.class)
    public TowerRunVo enter(String uid) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        Stage root = stageProgressService.requireModeRoot(StageModeCode.TOWER);
        List<Stage> ordered = stageProgressService.listOrderedLevels(root.getId(), true);
        ErrorFactory.throwError(ordered.isEmpty(), "无尽塔暂无关卡");

        PlayerTowerRun run = getByUid(uid);
        if (run == null) {
            run = new PlayerTowerRun();
            run.setUid(uid);
        }
        run.setStatus(TowerRunStatus.RUNNING);
        run.setCurrentLevelId(ordered.get(0).getId());
        run.setAllyHpJson(null);
        this.saveOrUpdate(run);
        return status(uid);
    }

    public Map<String, Integer> readAllyHp(PlayerTowerRun run) {
        if (run == null || Wx.isEmpty(run.getAllyHpJson())) {
            return null;
        }
        try {
            return MAPPER.readValue(run.getAllyHpJson(), new TypeReference<Map<String, Integer>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void writeAllyHp(PlayerTowerRun run, Map<String, Integer> hp) {
        try {
            run.setAllyHpJson(hp == null || hp.isEmpty() ? null : MAPPER.writeValueAsString(hp));
        } catch (Exception e) {
            run.setAllyHpJson(null);
        }
    }

    public Stage requireCurrentLevel(PlayerTowerRun run) {
        ErrorFactory.throwError(run == null || run.getStatus() != TowerRunStatus.RUNNING, "请先进入无尽塔");
        ErrorFactory.throwError(Wx.isEmpty(run.getCurrentLevelId()), "无尽塔进度异常");
        return stageService.requireLevel(run.getCurrentLevelId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void onWin(PlayerTowerRun run, Map<String, Integer> allyHp) {
        Stage root = stageProgressService.requireModeRoot(StageModeCode.TOWER);
        List<Stage> ordered = stageProgressService.listOrderedLevels(root.getId(), true);
        int idx = indexOf(ordered, run.getCurrentLevelId());
        ErrorFactory.throwError(idx < 0, "无尽塔进度异常");
        writeAllyHp(run, allyHp);
        if (idx >= ordered.size() - 1) {
            run.setStatus(TowerRunStatus.CLEARED);
            run.setCurrentLevelId(null);
        } else {
            run.setStatus(TowerRunStatus.RUNNING);
            run.setCurrentLevelId(ordered.get(idx + 1).getId());
        }
        this.updateById(run);
    }

    @Transactional(rollbackFor = Exception.class)
    public void onLose(PlayerTowerRun run) {
        run.setStatus(TowerRunStatus.DEAD);
        run.setAllyHpJson(null);
        this.updateById(run);
    }

    public void assertIsTowerLevel(Stage level) {
        Stage type = stageProgressService.resolveTypeRoot(level);
        ErrorFactory.throwError(!StageModeCode.TOWER.equals(type.getCode()), "不是无尽塔关卡");
    }

    private void fillLevelMeta(TowerRunVo vo, String levelId) {
        if (Wx.isEmpty(levelId)) {
            return;
        }
        Stage level = stageService.getById(levelId);
        if (level == null) {
            return;
        }
        vo.setCurrentLevelId(level.getId());
        vo.setCurrentLevelName(level.getName());
        vo.setCurrentLevelCode(level.getCode());
        Stage chapter = stageService.getById(level.getParentId());
        if (chapter != null) {
            vo.setChapterName(chapter.getName());
        }
    }

    private static int indexOf(List<Stage> ordered, String levelId) {
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(levelId)) {
                return i;
            }
        }
        return -1;
    }
}
