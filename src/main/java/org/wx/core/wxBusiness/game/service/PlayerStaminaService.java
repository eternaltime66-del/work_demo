package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.mapper.PlayerStaminaMapper;

@Service
public class PlayerStaminaService extends WxServiceImpl<PlayerStaminaMapper, PlayerStamina> {

    public static final int DEFAULT_MAX = 30;

    public PlayerStamina getOrCreate(String uid) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        PlayerStamina row = this.find().eq(PlayerStamina::getUid, uid).one();
        if (row != null) {
            return row;
        }
        row = new PlayerStamina();
        row.setUid(uid);
        row.setStamina(DEFAULT_MAX);
        row.setMaxStamina(DEFAULT_MAX);
        this.save(row);
        return row;
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerStamina consume(String uid, int cost) {
        PlayerStamina row = getOrCreate(uid);
        if (cost <= 0) {
            return row;
        }
        ErrorFactory.throwError(row.getStamina() == null || row.getStamina() < cost, "体力不足");
        row.setStamina(row.getStamina() - cost);
        this.updateById(row);
        return row;
    }

    /**
     * 后台赠送体力：累加当前体力，允许超过上限。
     */
    @Transactional(rollbackFor = Exception.class)
    public PlayerStamina add(String uid, int amount) {
        ErrorFactory.throwError(amount <= 0, "赠送体力须为正整数");
        PlayerStamina row = getOrCreate(uid);
        int cur = row.getStamina() == null ? 0 : row.getStamina();
        row.setStamina(cur + amount);
        this.updateById(row);
        return row;
    }
}
