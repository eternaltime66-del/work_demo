package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 锚点被动-战斗生命周期锚点。
 * <p>
 * 「充能技能」含普攻 / 大招 / 小技能。
 */
public enum PassiveAnchorType {
    /** 释放充能技能后 */
    AFTER_CAST_CHARGE("释放充能技能后"),
    /** 受到充能技能后 */
    AFTER_RECEIVE_CHARGE("受到充能技能后"),
    /** 受到充能技能伤害后 */
    AFTER_TAKE_CHARGE_DMG("受到充能技能伤害后"),
    /** 造成充能技能伤害后 */
    AFTER_DEAL_CHARGE_DMG("造成充能技能伤害后");

    private final String label;

    PassiveAnchorType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 是否需要技能匹配（任意 / 指定分类 / 指定技能） */
    public boolean needsSkillMatch() {
        return this == AFTER_CAST_CHARGE
                || this == AFTER_RECEIVE_CHARGE
                || this == AFTER_TAKE_CHARGE_DMG
                || this == AFTER_DEAL_CHARGE_DMG;
    }
}
