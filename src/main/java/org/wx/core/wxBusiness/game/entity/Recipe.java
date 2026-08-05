package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

import java.util.List;

/**
 * 合成配方
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_recipe")
@BizIdPrefix("RCP")
public class Recipe extends WxBaseEntity<Recipe> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 配方名称 */
    private String name;

    /** 产出物品 id */
    private String outputItemId;

    /** 产出数量 */
    private Integer outputQty;

    private Integer sort;

    /** 是否启用 */
    private Boolean enable;

    private String remark;

    @TableField(exist = false)
    private String outputItemName;

    @TableField(exist = false)
    private List<RecipeMaterial> materials;
}
