package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.mapper.ItemLegsMapper;

import java.util.List;

@Service
public class ItemLegsService extends WxServiceImpl<ItemLegsMapper, ItemLegs> {

    public List<ItemLegs> listByItemId(String itemId) {
        return this.find().eq(ItemLegs::getItemId, itemId).list();
    }

    public ItemLegs getByItemId(String itemId) {
        return this.find().eq(ItemLegs::getItemId, itemId).one();
    }
}
