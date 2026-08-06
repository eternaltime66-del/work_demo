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
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.PeriodicTriggerMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

import java.util.List;

/**
 * 被动技能主表
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

    /** OUT_BASIC / OUT_ADVANCED / ... */
    private PassiveSkillType passiveType;

    /** UNLIMITED / SELECT */
    private PassiveConditionMode conditionMode;

    /** IN_ANCHOR：生命周期锚点 */
    private PassiveAnchorType anchorType;

    /** IN_ANCHOR：技能匹配（释放/受到充能技能） */
    private SkillChargeMatchMode skillMatchMode;

    private ActiveSkillType refSkillType;

    private String refSkillId;

    /** IN_PERIODIC / IN_SUSTAINED：触发模式 */
    private PeriodicTriggerMode periodicTriggerMode;

    /** IN_PERIODIC / IN_SUSTAINED：上公式 */
    private String leftFormulaJson;

    /** IN_PERIODIC / IN_SUSTAINED：比较符 */
    private CompareOp compareOp;

    /** IN_PERIODIC / IN_SUSTAINED：下公式 */
    private String rightFormulaJson;

    /** IN_PERIODIC：本场最多触发次数；0/null=不限（持续效果不用） */
    private Integer maxTriggerPerBattle;

    private Integer sort;

    private Boolean enable;

    private String remark;

    private String more;

    /** 生效条件判定组（非表字段） */
    @TableField(exist = false)
    private List<PassiveCondition> conditions;

    /** 战斗外效果（OUT_*，非表字段） */
    @TableField(exist = false)
    private List<PassiveEffect> effects;

    /** 战斗内效果（IN_ANCHOR / IN_PERIODIC / IN_SUSTAINED，非表字段） */
    @TableField(exist = false)
    private List<PassiveCombatEffect> combatEffects;

    /** 列表展示 */
    @TableField(exist = false)
    private String stackModeLabel;
}
