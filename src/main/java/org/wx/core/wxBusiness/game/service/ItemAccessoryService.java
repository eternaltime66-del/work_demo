package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemAccessory;
import org.wx.core.wxBusiness.game.mapper.ItemAccessoryMapper;

import java.util.List;

@Service
public class ItemAccessoryService extends WxServiceImpl<ItemAccessoryMapper, ItemAccessory> {

    public List<ItemAccessory> listByItemId(String itemId) {
        return this.find().eq(ItemAccessory::getItemId, itemId).list();
    }

    public ItemAccessory getByItemId(String itemId) {
        return this.find().eq(ItemAccessory::getItemId, itemId).one();
    }
}
