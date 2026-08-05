package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

@Data
public class CraftMaterialVo {

    private String itemId;
    private String itemName;
    private String icon;
    private Integer requiredQty;
    private Integer ownedQty;
    private Integer missingQty;
    private Boolean enough;
    /** BATTLE / CRAFT / NONE */
    private String sourceType;
    /** 掉落 / 去合成 / 敬请期待 */
    private String sourceLabel;
    private String sourceRecipeId;
}
