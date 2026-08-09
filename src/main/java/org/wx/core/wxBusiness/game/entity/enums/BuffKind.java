package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 标准化 BUFF 类型。
 */
public enum BuffKind {
    /** 属性 BUFF */
    ATTR,
    /** 脉冲效果 BUFF */
    PULSE,
    /** 持续判定属性 BUFF */
    JUDGE_ATTR,
    /** 持续判定爆发 BUFF */
    JUDGE_BURST,
    /** 闪避：持有时按概率使匹配技能伤害为 0 */
    DODGE
}
