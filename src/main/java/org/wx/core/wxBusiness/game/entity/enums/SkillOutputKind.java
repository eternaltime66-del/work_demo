package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 最终输出模块类型（主动/被动共用）。
 */
public enum SkillOutputKind {
    /** 属性类 */
    ATTR,
    /** 效果类：伤害/治疗 */
    EFFECT,
    /** 追加类：挂载 BUFF（含闪避等） */
    APPEND_BUFF
}
