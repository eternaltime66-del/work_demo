package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 配方材料
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_recipe_material")
@BizIdPrefix("RCM")
public class RecipeMaterial extends WxBaseEntity<RecipeMaterial> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 配方 id */
    private String recipeId;

    /** 材料物品 id */
    private String itemId;

    /** 数量 */
    private Integer quantity;

    private Integer sort;

    @TableField(exist = false)
    private String itemName;
}
