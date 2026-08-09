package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 技能充能：触发事件
 */
public enum SkillChargeEvent {
    /** 释放技能时 */
    CAST,
    /** 受到技能时 */
    RECEIVE,
    /** 技能造成伤害时 */
    DEAL_DAMAGE,
    /** 受到技能伤害时 */
    TAKE_DAMAGE,
    /** 技能造成击杀时 */
    KILL
}
