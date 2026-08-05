package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.mapper.ItemHelmetMapper;

import java.util.List;

@Service
public class ItemHelmetService extends WxServiceImpl<ItemHelmetMapper, ItemHelmet> {

    public List<ItemHelmet> listByItemId(String itemId) {
        return this.find().eq(ItemHelmet::getItemId, itemId).list();
    }

    public ItemHelmet getByItemId(String itemId) {
        return this.find().eq(ItemHelmet::getItemId, itemId).one();
    }
}
