package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.mapper.ItemGlovesMapper;

import java.util.List;

@Service
public class ItemGlovesService extends WxServiceImpl<ItemGlovesMapper, ItemGloves> {

    public List<ItemGloves> listByItemId(String itemId) {
        return this.find().eq(ItemGloves::getItemId, itemId).list();
    }

    public ItemGloves getByItemId(String itemId) {
        return this.find().eq(ItemGloves::getItemId, itemId).one();
    }
}
