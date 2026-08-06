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

    /** 默认充能技能槽数量（装备用） */
    private Integer chargeSkillSlotCount;

    /** 玩家默认可编辑充能技能槽数量 */
    private Integer playerDefaultEditChargeSkillSlotCount;

    /** 玩家最大可编辑充能技能槽数量 */
    private Integer playerMaxEditChargeSkillSlotCount;

    /** 默认自带基础被动数量 */
    private Integer basicPassiveSlotCount;

    /** 默认可编辑基础被动数量 */
    private Integer playerDefaultEditBasicPassiveSlotCount;

    /** 可编辑最大基础被动数量 */
    private Integer playerMaxEditBasicPassiveSlotCount;

    /** 默认自带高级属性被动数量 */
    private Integer advancedPassiveSlotCount;

    /** 默认可编辑高级属性被动数量 */
    private Integer playerDefaultEditAdvancedPassiveSlotCount;

    /** 可编辑最大高级属性被动数量 */
    private Integer playerMaxEditAdvancedPassiveSlotCount;

    /** 默认自带锚点被动数量 */
    private Integer anchorPassiveSlotCount;

    /** 默认可编辑锚点被动数量 */
    private Integer playerDefaultEditAnchorPassiveSlotCount;

    /** 可编辑最大锚点被动数量 */
    private Integer playerMaxEditAnchorPassiveSlotCount;

    /** 默认自带周期被动数量 */
    private Integer periodicPassiveSlotCount;

    /** 默认可编辑周期被动数量 */
    private Integer playerDefaultEditPeriodicPassiveSlotCount;

    /** 可编辑最大周期被动数量 */
    private Integer playerMaxEditPeriodicPassiveSlotCount;

    /** 默认自带持续效果被动数量 */
    private Integer sustainedPassiveSlotCount;

    /** 默认可编辑持续效果被动数量 */
    private Integer playerDefaultEditSustainedPassiveSlotCount;

    /** 可编辑最大持续效果被动数量 */
    private Integer playerMaxEditSustainedPassiveSlotCount;
}
