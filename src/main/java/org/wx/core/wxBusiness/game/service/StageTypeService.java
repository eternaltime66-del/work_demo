package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.StageChapter;
import org.wx.core.wxBusiness.game.entity.StageType;
import org.wx.core.wxBusiness.game.mapper.StageTypeMapper;

import java.util.List;

@Service
public class StageTypeService extends WxServiceImpl<StageTypeMapper, StageType> {

    @Resource
    private StageChapterService stageChapterService;

    @Transactional(rollbackFor = Exception.class)
    public void removeCascade(String typeId) {
        List<StageChapter> chapters = stageChapterService.find()
                .eq(StageChapter::getTypeId, typeId)
                .list();
        for (StageChapter chapter : chapters) {
            stageChapterService.removeCascade(chapter.getId());
        }
        this.removeById(typeId);
    }
}
