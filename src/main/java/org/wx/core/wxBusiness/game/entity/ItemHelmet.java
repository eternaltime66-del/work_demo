package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

import java.math.BigDecimal;

/**
 * 物品-头盔扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_helmet")
@BizIdPrefix("HEL")
public class ItemHelmet extends WxBaseEntity<ItemHelmet> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品主表 id */
    private String itemId;

    /** 生命 */
    private Integer hp;

    /** 防御力 */
    private Integer defense;

    /** 增加攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedUpRatio;

    /** 减少攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedDownRatio;

    private String remark;
}
