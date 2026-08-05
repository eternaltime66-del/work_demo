package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.mapper.ItemArmorMapper;

import java.util.List;

@Service
public class ItemArmorService extends WxServiceImpl<ItemArmorMapper, ItemArmor> {

    public List<ItemArmor> listByItemId(String itemId) {
        return this.find().eq(ItemArmor::getItemId, itemId).list();
    }

    public ItemArmor getByItemId(String itemId) {
        return this.find().eq(ItemArmor::getItemId, itemId).one();
    }
}
