package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;

import java.math.BigDecimal;

/**
 * 被动技能-效果（目标固定自己；基础/高级属性增减）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_passive_effect")
@BizIdPrefix("PSE")
public class PassiveEffect extends WxBaseEntity<PassiveEffect> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String skillId;

    /** 效果属性（基础或高级） */
    private PassiveEffectAttrKey attrKey;

    private AttrModifyDirection attrDir;

    /** 增减数值：基础为绝对值；高级多为百分比（1%） */
    @TableField("value_num")
    private BigDecimal valueNum;

    private Integer sort;

    private String remark;

    private String more;
}
