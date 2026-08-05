package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.mapper.ItemWeaponMapper;

import java.util.List;

@Service
public class ItemWeaponService extends WxServiceImpl<ItemWeaponMapper, ItemWeapon> {

    public List<ItemWeapon> listByItemId(String itemId) {
        return this.find().eq(ItemWeapon::getItemId, itemId).list();
    }

    public ItemWeapon getByItemId(String itemId) {
        return this.find().eq(ItemWeapon::getItemId, itemId).one();
    }
}
