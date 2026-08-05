package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ItemMaterial;
import org.wx.core.wxBusiness.game.mapper.ItemMaterialMapper;

import java.util.List;

@Service
public class ItemMaterialService extends WxServiceImpl<ItemMaterialMapper, ItemMaterial> {

    public List<ItemMaterial> listByItemId(String itemId) {
        return this.find().eq(ItemMaterial::getItemId, itemId).list();
    }

    public ItemMaterial getByItemId(String itemId) {
        return this.find().eq(ItemMaterial::getItemId, itemId).one();
    }
}
