package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 技能匹配范围（充能条件 / 被动战斗事件 / 公式读取技能次数 共用）
 */
public enum SkillChargeMatchMode {
    /** 任意技能 */
    ANY,
    /** 指定技能类型（普攻/大招/小技能） */
    ANY_TYPE,
    /** 指定流派 */
    ANY_SCHOOL,
    /** 指定元素类型 */
    ANY_ELEMENT,
    /** 指定技能 */
    SPECIFIC
}
