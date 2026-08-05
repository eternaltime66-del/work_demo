package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 充能条件类型（可继续扩展）
 */
public enum ChargeConditionType {
    /** 行动值：全局每经过 x 行动值增加 y 点充能 */
    ACTION_VALUE,
    /** 技能充能：释放/受到 某技能（或类型）时增加充能 */
    SKILL_CHARGE
}
