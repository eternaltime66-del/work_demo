package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CraftRecipeVo {

    private String id;
    private String name;
    private String remark;
    private String resultItemId;
    private String resultItemName;
    private String resultItemIcon;
    /** 产出物品类型：WEAPON / ARMOR / ... */
    private String resultItemType;
    private Integer resultQty;
    /** 产出物品详情（属性/攻速等） */
    private CraftItemDetailVo resultItem;
    private List<CraftMaterialVo> materials = new ArrayList<>();
    private List<CraftMaterialVo> missingMaterials = new ArrayList<>();
    private Boolean canCraft;
}
