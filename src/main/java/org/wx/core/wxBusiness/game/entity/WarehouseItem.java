package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;

import java.math.BigDecimal;

/**
 * 仓库物品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_warehouse_item")
@BizIdPrefix("WHI")
public class WarehouseItem extends WxBaseEntity<WarehouseItem> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 玩家 uid */
    private String uid;

    /** 仓库 id */
    private String warehouseId;

    /** 物品 id */
    private String itemId;

    /** 数量 */
    private Integer quantity;

    /** 格子号 */
    private Integer slotNo;

    @TableField(exist = false)
    private String itemName;

    @TableField(exist = false)
    private String icon;

    @TableField(exist = false)
    private ItemType itemType;

    @TableField(exist = false)
    private Integer maxStack;

    @TableField(exist = false)
    private BigDecimal weight;

    /** 物品详情（属性/槽位/默认技能与被动） */
    @TableField(exist = false)
    private CraftItemDetailVo detail;
}
