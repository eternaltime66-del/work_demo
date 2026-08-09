package org.wx.core.wxBusiness.api.admin;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.enums.StageKind;
import org.wx.core.wxBusiness.game.entity.vo.StageFirstRewardVo;
import org.wx.core.wxBusiness.game.service.StageFirstRewardService;
import org.wx.core.wxBusiness.game.service.StageLevelMonsterService;
import org.wx.core.wxBusiness.game.service.StageService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;
import java.util.Map;

/**
 * 后台-关卡树（单表：分类 / 大关 / 小关）
 */
@RestController
@RequestMapping("/back/stage")
public class B10StageController {

    @Resource
    public StageService stageService;
    @Resource
    public StageLevelMonsterService stageLevelMonsterService;
    @Resource
    public StageFirstRewardService stageFirstRewardService;

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<Stage> update(@RequestBody Stage entity) {
        entity.clearEmptyString();
        stageService.saveNode(entity);
        return WxResult.success(entity);
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody Stage entity) {
        stageService.removeCascade(entity.getId());
        return WxResult.success();
    }

    /**
     * 按父节点列子节点；parentId 空 = 根分类
     */
    @PostMapping("/children")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Stage>> children(String parentId) {
        return WxResult.success(stageService.listChildren(parentId, false));
    }

    @PostMapping("/tree")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Map<String, Object>>> tree() {
        return WxResult.success(stageService.buildLegacyTree(false));
    }

    // ---------- 首通奖励 ----------

    @PostMapping("/firstReward/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageFirstRewardVo>> firstRewardList(@ParamCheck String stageId) {
        return WxResult.success(stageFirstRewardService.listVoByStageId(stageId));
    }

    @PostMapping("/firstReward/replace")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> firstRewardReplace(@RequestBody Map<String, Object> body) {
        Object stageIdObj = body != null ? body.get("stageId") : null;
        String stageId = stageIdObj != null ? String.valueOf(stageIdObj) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rewards = body != null && body.get("rewards") instanceof List
                ? (List<Map<String, Object>>) body.get("rewards")
                : List.of();
        stageFirstRewardService.replaceRewards(stageId, rewards);
        return WxResult.success();
    }

    // ---------- 小关怪物位置 ----------

    @PostMapping("/level/monster/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageLevelMonster>> levelMonsterList(@ParamCheck String levelId) {
        return WxResult.success(stageLevelMonsterService.listByLevelId(levelId));
    }

    @PostMapping("/level/monster/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelMonsterUpdate(@RequestBody StageLevelMonster entity) {
        entity.clearEmptyString();
        stageLevelMonsterService.saveOne(entity);
        return WxResult.success();
    }

    @PostMapping("/level/monster/add")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<StageLevelMonster> levelMonsterAdd(
            @ParamCheck String levelId,
            @ParamCheck String monsterId
    ) {
        return WxResult.success(stageLevelMonsterService.addRandom(levelId, monsterId));
    }

    @PostMapping("/level/monster/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelMonsterRemove(@RequestBody StageLevelMonster entity) {
        stageLevelMonsterService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 兼容旧路径 ----------

    @PostMapping("/type/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> typeUpdate(@RequestBody Stage entity) {
        entity.setKind(StageKind.TYPE);
        entity.setParentId(null);
        entity.clearEmptyString();
        stageService.saveNode(entity);
        return WxResult.success();
    }

    @PostMapping("/type/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> typeRemove(@RequestBody Stage entity) {
        stageService.removeCascade(entity.getId());
        return WxResult.success();
    }

    @PostMapping("/chapter/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chapterUpdate(@RequestBody Stage entity) {
        entity.setKind(StageKind.CHAPTER);
        entity.clearEmptyString();
        stageService.saveNode(entity);
        return WxResult.success();
    }

    @PostMapping("/chapter/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chapterRemove(@RequestBody Stage entity) {
        stageService.removeCascade(entity.getId());
        return WxResult.success();
    }

    @PostMapping("/level/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelUpdate(@RequestBody Stage entity) {
        entity.setKind(StageKind.LEVEL);
        entity.clearEmptyString();
        stageService.saveNode(entity);
        return WxResult.success();
    }

    @PostMapping("/level/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelRemove(@RequestBody Stage entity) {
        stageService.removeCascade(entity.getId());
        return WxResult.success();
    }
}
