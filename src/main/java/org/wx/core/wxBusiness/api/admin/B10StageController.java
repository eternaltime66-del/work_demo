package org.wx.core.wxBusiness.api.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
 * 后台-关卡三级联动（类型 / 大关 / 小关）
 */
@RestController
@RequestMapping("/back/stage")
public class B10StageController {

    @Resource
    public StageTypeService stageTypeService;
    @Resource
    public StageChapterService stageChapterService;
    @Resource
    public StageLevelService stageLevelService;
    @Resource
    public StageLevelMonsterService stageLevelMonsterService;

    // ---------- 类型 ----------

    @PostMapping("/type/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageType>> typeList(@RequestBody StageType entity) {
        entity.clearEmptyString();
        IPage<StageType> page = stageTypeService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/type/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> typeUpdate(@RequestBody StageType entity) {
        entity.clearEmptyString();
        stageTypeService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/type/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> typeRemove(@RequestBody StageType entity) {
        stageTypeService.removeCascade(entity.getId());
        return WxResult.success();
    }

    // ---------- 大关 ----------

    @PostMapping("/chapter/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageChapter>> chapterList(@RequestBody StageChapter entity) {
        entity.clearEmptyString();
        IPage<StageChapter> page = stageChapterService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/chapter/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chapterUpdate(@RequestBody StageChapter entity) {
        entity.clearEmptyString();
        stageChapterService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/chapter/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chapterRemove(@RequestBody StageChapter entity) {
        stageChapterService.removeCascade(entity.getId());
        return WxResult.success();
    }

    // ---------- 小关 ----------

    @PostMapping("/level/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageLevel>> levelList(@RequestBody StageLevel entity) {
        entity.clearEmptyString();
        IPage<StageLevel> page = stageLevelService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/level/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelUpdate(@RequestBody StageLevel entity) {
        entity.clearEmptyString();
        stageLevelService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/level/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> levelRemove(@RequestBody StageLevel entity) {
        stageLevelMonsterService.removeByLevelId(entity.getId());
        stageLevelService.removeById(entity.getId());
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

    /**
     * 添加怪物到小关：随机找可放置格子
     */
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

    /**
     * 三级树：类型 -> 大关 -> 小关
     */
    @PostMapping("/tree")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Map<String, Object>>> tree() {
        return WxResult.success(buildTree(false));
    }

    /**
     * 按类型查大关（联动）
     */
    @PostMapping("/chapter/byType")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageChapter>> chapterByType(@ParamCheck String typeId) {
        return WxResult.success(stageChapterService.listByTypeId(typeId));
    }

    /**
     * 按大关查小关（联动）
     */
    @PostMapping("/level/byChapter")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<StageLevel>> levelByChapter(@ParamCheck String chapterId) {
        return WxResult.success(stageLevelService.listByChapterId(chapterId));
    }

    List<Map<String, Object>> buildTree(boolean onlyEnable) {
        List<StageType> types = onlyEnable
                ? stageTypeService.find().eq(StageType::getEnable, true).orderByAsc(StageType::getSort).list()
                : stageTypeService.find().orderByAsc(StageType::getSort).list();
        List<Map<String, Object>> tree = new ArrayList<>();
        for (StageType type : types) {
            Map<String, Object> typeNode = new HashMap<>();
            typeNode.put("id", type.getId());
            typeNode.put("name", type.getName());
            typeNode.put("code", type.getCode());
            List<Map<String, Object>> chapterNodes = new ArrayList<>();
            List<StageChapter> chapters = stageChapterService.listByTypeId(type.getId());
            if (!onlyEnable) {
                chapters = stageChapterService.find()
                        .eq(StageChapter::getTypeId, type.getId())
                        .orderByAsc(StageChapter::getSort)
                        .list();
            }
            for (StageChapter chapter : chapters) {
                Map<String, Object> chapterNode = new HashMap<>();
                chapterNode.put("id", chapter.getId());
                chapterNode.put("name", chapter.getName());
                chapterNode.put("code", chapter.getCode());
                List<StageLevel> levels = onlyEnable
                        ? stageLevelService.listByChapterId(chapter.getId())
                        : stageLevelService.find()
                        .eq(StageLevel::getChapterId, chapter.getId())
                        .orderByAsc(StageLevel::getSort)
                        .list();
                chapterNode.put("levels", levels);
                chapterNodes.add(chapterNode);
            }
            typeNode.put("chapters", chapterNodes);
            tree.add(typeNode);
        }
        return tree;
    }
}
