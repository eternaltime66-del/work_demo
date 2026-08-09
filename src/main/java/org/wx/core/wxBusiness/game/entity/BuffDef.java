package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.BuffStackMode;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;

/**
 * 标准化 BUFF 定义（技能 V2）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_buff_def")
@BizIdPrefix("BFD")
public class BuffDef extends WxBaseEntity<BuffDef> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String code;

    private BuffKind buffKind;

    /** true=增益 false=减益 */
    private Boolean beneficial;

    private Boolean dispelable;

    private BuffStackMode stackMode;

    /** 最大层数；0/null=不限（仅 STACK） */
    private Integer maxStacks;

    /** 持续行动值；0/null=战斗内永久 */
    private Integer durationAv;

    /** ATTR / JUDGE_ATTR：属性键 */
    private AttrModifyKey attrKey;

    private AttrModifyDirection attrDir;

    private String formulaJson;

    /** PULSE：间隔行动值 */
    private Integer pulseEveryAv;

    /** PULSE / JUDGE_BURST：伤害或治疗 */
    private SkillEffectType pulseEffectType;

    private DamageElement damageElement;

    private SkillEffectTarget pulseTargetType;

    /** JUDGE_*：判定公式 */
    private String leftFormulaJson;

    private CompareOp compareOp;

    private String rightFormulaJson;

    /** DODGE：持有时闪避概率（单位 1%，1~100） */
    private Integer dodgeChance;

    /** DODGE：技能匹配模式（与充能/被动同一套五维筛选） */
    private SkillChargeMatchMode skillMatchMode;

    private String matchSkillSchool;

    private ActiveSkillType matchSkillType;

    private DamageElement matchDamageElement;

    private String matchSkillId;

    private Integer sort;

    private Boolean enable;

    private String remark;

    private String more;
}
