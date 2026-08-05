package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.StageChapter;
import org.wx.core.wxBusiness.game.entity.StageLevel;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.StageType;
import org.wx.core.wxBusiness.game.service.StageChapterService;
import org.wx.core.wxBusiness.game.service.StageLevelMonsterService;
import org.wx.core.wxBusiness.game.service.StageLevelService;
import org.wx.core.wxBusiness.game.service.StageTypeService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端-关卡三级联动
 */
@RestController
@RequestMapping("/api/stage")
public class A5StageController {

    @Resource
    public StageTypeService stageTypeService;
    @Resource
    public StageChapterService stageChapterService;
    @Resource
    public StageLevelService stageLevelService;
    @Resource
    public StageLevelMonsterService stageLevelMonsterService;

    @PostMapping("/type/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<StageType>> typeList() {
        return WxResult.success(
                stageTypeService.find().eq(StageType::getEnable, true).orderByAsc(StageType::getSort).list()
        );
    }

    @PostMapping("/chapter/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<StageChapter>> chapterList(@ParamCheck String typeId) {
        return WxResult.success(stageChapterService.listByTypeId(typeId));
    }

    @PostMapping("/level/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<StageLevel>> levelList(@ParamCheck String chapterId) {
        return WxResult.success(stageLevelService.listByChapterId(chapterId));
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
     * 完整三级树（仅启用）
     */
    @PostMapping("/tree")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Map<String, Object>>> tree() {
        List<StageType> types = stageTypeService.find()
                .eq(StageType::getEnable, true)
                .orderByAsc(StageType::getSort)
                .list();
        List<Map<String, Object>> tree = new ArrayList<>();
        for (StageType type : types) {
            Map<String, Object> typeNode = new HashMap<>();
            typeNode.put("id", type.getId());
            typeNode.put("name", type.getName());
            typeNode.put("code", type.getCode());
            List<Map<String, Object>> chapterNodes = new ArrayList<>();
            for (StageChapter chapter : stageChapterService.listByTypeId(type.getId())) {
                Map<String, Object> chapterNode = new HashMap<>();
                chapterNode.put("id", chapter.getId());
                chapterNode.put("name", chapter.getName());
                chapterNode.put("code", chapter.getCode());
                chapterNode.put("levels", stageLevelService.listByChapterId(chapter.getId()));
                chapterNodes.add(chapterNode);
            }
            typeNode.put("chapters", chapterNodes);
            tree.add(typeNode);
        }
        return WxResult.success(tree);
    }
}
