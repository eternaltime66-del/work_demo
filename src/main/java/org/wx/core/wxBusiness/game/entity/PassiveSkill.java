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
import org.wx.core.wxBusiness.game.entity.enums.BattleStartApplyRule;
import org.wx.core.wxBusiness.game.entity.enums.CombatEventType;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.PeriodicTriggerMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

import java.util.List;

/**
 * 被动技能主表（技能 V2）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_passive_skill")
@BizIdPrefix("PSK")
public class PassiveSkill extends WxBaseEntity<PassiveSkill> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String code;

    private PassiveSkillType passiveType;

    private PassiveConditionMode conditionMode;

    /** @deprecated 旧 IN_ANCHOR */
    @Deprecated
    private PassiveAnchorType anchorType;

    private SkillChargeMatchMode skillMatchMode;

    private ActiveSkillType refSkillType;

    /** skillMatchMode=ANY_SCHOOL */
    private String refSkillSchool;

    /** skillMatchMode=ANY_ELEMENT */
    private DamageElement refDamageElement;

    private String refSkillId;

    /** @deprecated 旧周期模式；判定/脉冲改用 SELF 语义 */
    @Deprecated
    private PeriodicTriggerMode periodicTriggerMode;

    private String leftFormulaJson;

    private CompareOp compareOp;

    private String rightFormulaJson;

    private Integer maxTriggerPerBattle;

    /** BATTLE_COMBAT */
    private CombatEventType combatEvent;

    /** BATTLE_START */
    private BattleStartApplyRule startApplyRule;

    /** 开战时间规则：行动值阈值 / 间隔 */
    private Integer startElapsedAv;

    private Integer sort;

    private Boolean enable;

    private String remark;

    private String more;

    @TableField(exist = false)
    private List<PassiveCondition> conditions;

    @TableField(exist = false)
    private List<PassiveEffect> effects;

    /** @deprecated 旧战斗效果；V2 用 outputs */
    @Deprecated
    @TableField(exist = false)
    private List<PassiveCombatEffect> combatEffects;

    @TableField(exist = false)
    private List<SkillOutput> outputs;

    @TableField(exist = false)
    private String stackModeLabel;
}
