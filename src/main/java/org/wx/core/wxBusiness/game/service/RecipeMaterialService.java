package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.RecipeMaterial;
import org.wx.core.wxBusiness.game.mapper.RecipeMaterialMapper;

import java.util.List;

@Service
public class RecipeMaterialService extends WxServiceImpl<RecipeMaterialMapper, RecipeMaterial> {

    @Resource
    private ItemService itemService;

    public List<RecipeMaterial> listByRecipeId(String recipeId) {
        List<RecipeMaterial> list = this.find()
                .eq(RecipeMaterial::getRecipeId, recipeId)
                .orderByAsc(RecipeMaterial::getSort)
                .list();
        fillItemName(list);
        return list;
    }

    public void fillItemName(List<RecipeMaterial> list) {
        if (list == null) {
            return;
        }
        for (RecipeMaterial row : list) {
            Item item = itemService.getById(row.getItemId());
            if (item != null) {
                row.setItemName(item.getName());
            }
        }
    }
}
