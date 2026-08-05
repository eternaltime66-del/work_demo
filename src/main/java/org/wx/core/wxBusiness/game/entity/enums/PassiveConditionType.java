package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 被动生效条件类型
 */
public enum PassiveConditionType {
    /** 装备了指定装备 */
    EQUIP_ITEM,
    /** 装备了指定类型装备 */
    EQUIP_ITEM_TYPE,
    /** 装备了指定技能 */
    EQUIP_SKILL,
    /** 装备了指定类型技能 */
    EQUIP_SKILL_TYPE,
    /** 公式判定（左公式 比较符 右公式） */
    FORMULA_COMPARE;

    public String label() {
        return switch (this) {
            case EQUIP_ITEM -> "装备了指定装备";
            case EQUIP_ITEM_TYPE -> "装备了指定类型装备";
            case EQUIP_SKILL -> "装备了指定技能";
            case EQUIP_SKILL_TYPE -> "装备了指定类型技能";
            case FORMULA_COMPARE -> "公式判定";
        };
    }
}
