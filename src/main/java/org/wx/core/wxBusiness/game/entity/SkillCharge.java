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
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeScope;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

/**
 * 主动技能-充能条件（1 技能可多条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_skill_charge")
@BizIdPrefix("SCH")
public class SkillCharge extends WxBaseEntity<SkillCharge> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 主动技能 id */
    private String skillId;

    /** 充能条件名 */
    private String name;

    /** 条件类型 */
    private ChargeConditionType conditionType;

    /** 作用域（当前先用 GLOBAL） */
    private ChargeScope scope;

    /** ACTION_VALUE：每经过 x 行动值 */
    private Integer everyActionValue;

    /** 增加 y 点充能（各条件类型通用） */
    private Integer chargeGain;

    /** SKILL_CHARGE：释放 / 受到 / 造成伤害 / 受到伤害 / 造成击杀 */
    private SkillChargeEvent skillChargeEvent;

    /** SKILL_CHARGE：任意 / 指定类型 / 指定流派 / 指定元素 / 指定技能 */
    private SkillChargeMatchMode skillChargeMatch;

    /** SKILL_CHARGE + ANY_TYPE：匹配的技能类型 */
    private ActiveSkillType matchSkillType;

    /** SKILL_CHARGE + ANY_SCHOOL：匹配的流派 */
    private String matchSkillSchool;

    /** SKILL_CHARGE + ANY_ELEMENT：匹配的元素类型 */
    private DamageElement matchDamageElement;

    /** SKILL_CHARGE + SPECIFIC：匹配的技能 id */
    private String matchSkillId;

    /** 展示用：指定技能名称（非表字段） */
    @TableField(exist = false)
    private String matchSkillName;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 扩展 JSON */
    private String more;
}
