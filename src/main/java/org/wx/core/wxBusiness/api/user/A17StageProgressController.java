package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.entity.vo.StageLevelPreviewVo;
import org.wx.core.wxBusiness.game.entity.vo.StageProgressVo;
import org.wx.core.wxBusiness.game.entity.vo.TowerRunVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.service.BattleService;
import org.wx.core.wxBusiness.game.service.PlayerStaminaService;
import org.wx.core.wxBusiness.game.service.StageProgressService;
import org.wx.core.wxBusiness.game.service.TowerRunService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 主线进度 / 体力 / 无尽塔
 */
@RestController
@RequestMapping("/api/stage")
public class A17StageProgressController {

    @Resource
    private StageProgressService stageProgressService;
    @Resource
    private PlayerStaminaService playerStaminaService;
    @Resource
    private TowerRunService towerRunService;
    @Resource
    private BattleService battleService;

    @PostMapping("/stamina")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<Map<String, Object>> stamina() {
        PlayerStamina ps = playerStaminaService.getOrCreate(Wx.memberId());
        Map<String, Object> m = new HashMap<>();
        m.put("stamina", ps.getStamina());
        m.put("maxStamina", ps.getMaxStamina());
        return WxResult.success(m);
    }

    /** 主线树 + 解锁 / 首通 / 体力消耗 / 今日次数 */
    @PostMapping("/main/progress")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<StageProgressVo> mainProgress() {
        return WxResult.success(stageProgressService.buildMainProgressTree(Wx.memberId()));
    }

    /** 关卡预览：是否首通、首通奖励领取态、怪物掉落清单 */
    @PostMapping("/level/preview")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<StageLevelPreviewVo> levelPreview(@ParamCheck(msg = "关卡ID") String levelId) {
        return WxResult.success(stageProgressService.buildLevelPreview(Wx.memberId(), levelId));
    }

    @PostMapping("/tower/status")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<TowerRunVo> towerStatus() {
        return WxResult.success(towerRunService.status(Wx.memberId()));
    }

    /** 进入无尽塔：从 1-1 重新开始 */
    @PostMapping("/tower/enter")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<TowerRunVo> towerEnter() {
        return WxResult.success(towerRunService.enter(Wx.memberId()));
    }

    /** 打无尽塔当前层 */
    @PostMapping("/tower/fight")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<BattleResultVo> towerFight() {
        return WxResult.success(battleService.towerFight(Wx.memberId()));
    }
}
