package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.enums.StageKind;
import org.wx.core.wxBusiness.game.service.StageLevelMonsterService;
import org.wx.core.wxBusiness.game.service.StageService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;
import java.util.Map;

/**
 * 前端-关卡树
 */
@RestController
@RequestMapping("/api/stage")
public class A5StageController {

    @Resource
    public StageService stageService;
    @Resource
    public StageLevelMonsterService stageLevelMonsterService;

    @PostMapping("/type/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Stage>> typeList() {
        return WxResult.success(stageService.listByKind(StageKind.TYPE, true));
    }

    @PostMapping("/chapter/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Stage>> chapterList(@ParamCheck String typeId) {
        return WxResult.success(stageService.listChildren(typeId, true));
    }

    @PostMapping("/level/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Stage>> levelList(@ParamCheck String chapterId) {
        return WxResult.success(stageService.listChildren(chapterId, true));
    }

    @PostMapping("/children")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Stage>> children(String parentId) {
        return WxResult.success(stageService.listChildren(parentId, true));
    }

    /**
     * 关卡怪物与位置（敌方 横6竖5）
     */
    @PostMapping("/level/monster/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<StageLevelMonster>> levelMonsterList(@ParamCheck String levelId) {
        return WxResult.success(stageLevelMonsterService.listByLevelId(levelId));
    }

    /**
     * 完整树（仅启用）：types[].chapters[].levels
     */
    @PostMapping("/tree")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Map<String, Object>>> tree() {
        return WxResult.success(stageService.buildLegacyTree(true));
    }
}
