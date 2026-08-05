package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.StageLevel;
import org.wx.core.wxBusiness.game.mapper.StageLevelMapper;

import java.util.List;

@Service
public class StageLevelService extends WxServiceImpl<StageLevelMapper, StageLevel> {

    public List<StageLevel> listByChapterId(String chapterId) {
        return this.find()
                .eq(StageLevel::getChapterId, chapterId)
                .eq(StageLevel::getEnable, true)
                .orderByAsc(StageLevel::getSort)
                .list();
    }
}
