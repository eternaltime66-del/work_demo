package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 伤害来源分类（锚点触发判定用）。
 */
public enum DamageSourceKind {
    /** 主动技能伤害 */
    ACTIVE_SKILL,
    /** 被动即时伤害 */
    PASSIVE,
    /** 脉冲 BUFF 周期伤害 */
    PULSE_BUFF
}
