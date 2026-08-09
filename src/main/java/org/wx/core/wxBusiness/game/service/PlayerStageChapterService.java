package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PlayerStageChapter;
import org.wx.core.wxBusiness.game.mapper.PlayerStageChapterMapper;

import java.util.List;

@Service
public class PlayerStageChapterService extends WxServiceImpl<PlayerStageChapterMapper, PlayerStageChapter> {

    public PlayerStageChapter getOrCreate(String uid, String chapterId) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        ErrorFactory.throwError(Wx.isEmpty(chapterId), "大关不能为空");
        PlayerStageChapter row = this.find()
                .eq(PlayerStageChapter::getUid, uid)
                .eq(PlayerStageChapter::getChapterId, chapterId)
                .one();
        if (row != null) {
            return row;
        }
        row = new PlayerStageChapter();
        row.setUid(uid);
        row.setChapterId(chapterId);
        row.setCleared(false);
        row.setFirstRewardClaimed(false);
        this.save(row);
        return row;
    }

    public List<PlayerStageChapter> listByUid(String uid) {
        if (Wx.isEmpty(uid)) {
            return List.of();
        }
        return this.find().eq(PlayerStageChapter::getUid, uid).list();
    }
}
