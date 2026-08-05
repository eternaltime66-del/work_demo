package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;

import java.math.BigDecimal;

/**
 * 物品主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item")
@BizIdPrefix("ITM")
public class Item extends WxBaseEntity<Item> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品编码 */
    private String code;

    /** 物品名称 */
    private String name;

    /** 图标 */
    private String icon;

    /** 物品类型 */
    private ItemType itemType;

    /** 最大堆叠 */
    private Integer maxStack;

    /** 重量 */
    private BigDecimal weight;

    private Integer sort;

    /** 是否启用 */
    private Boolean enable;

    private String remark;

    /** 充能技能槽数量（装备用） */
    private Integer chargeSkillSlotCount;

    /** 玩家是否可编辑技能槽（装备用） */
    private Boolean playerCanEditSkillSlot;
}
