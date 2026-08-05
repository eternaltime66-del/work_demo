package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.StageChapter;
import org.wx.core.wxBusiness.game.entity.StageLevel;
import org.wx.core.wxBusiness.game.mapper.StageChapterMapper;

import java.util.List;

@Service
public class StageChapterService extends WxServiceImpl<StageChapterMapper, StageChapter> {

    @Resource
    private StageLevelService stageLevelService;
    @Resource
    private StageLevelMonsterService stageLevelMonsterService;

    public List<StageChapter> listByTypeId(String typeId) {
        return this.find()
                .eq(StageChapter::getTypeId, typeId)
                .eq(StageChapter::getEnable, true)
                .orderByAsc(StageChapter::getSort)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeCascade(String chapterId) {
        List<StageLevel> levels = stageLevelService.find()
                .eq(StageLevel::getChapterId, chapterId)
                .list();
        for (StageLevel level : levels) {
            stageLevelMonsterService.removeByLevelId(level.getId());
            stageLevelService.removeById(level.getId());
        }
        this.removeById(chapterId);
    }
}
