package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;

/**
 * 被动技能-生效条件（判定组条目）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_passive_condition")
@BizIdPrefix("PCD")
public class PassiveCondition extends WxBaseEntity<PassiveCondition> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String skillId;

    private PassiveConditionType conditionType;

    /** EQUIP_ITEM */
    private String refItemId;

    /** EQUIP_ITEM_TYPE */
    private ItemType refItemType;

    /** EQUIP_SKILL */
    private String refSkillId;

    /** EQUIP_SKILL_TYPE */
    private ActiveSkillType refSkillType;

    /** FORMULA_COMPARE */
    private String leftFormulaJson;

    private CompareOp compareOp;

    private String rightFormulaJson;

    private Integer sort;

    private String remark;

    private String more;

    @TableField(exist = false)
    private String refItemName;

    @TableField(exist = false)
    private String refSkillName;
}
