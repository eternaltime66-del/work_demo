package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.mapper.ItemMapper;

@Service
public class ItemService extends WxServiceImpl<ItemMapper, Item> {
}
