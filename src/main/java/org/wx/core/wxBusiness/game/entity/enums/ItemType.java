package org.wx.core.wxBusiness.game.entity.enums;

/** 物品类型。 */
public enum ItemType {
    MATERIAL,
    WEAPON,
    ARMOR,
    GLOVES,
    HELMET,
    ACCESSORY,
    /** 装入技能槽后提供主动技能的特殊装备。 */
    SKILL_STONE,
    LEGS;

    public String label() {
        return switch (this) {
            case MATERIAL -> "材料";
            case WEAPON -> "武器";
            case ARMOR -> "护甲";
            case GLOVES -> "护手";
            case HELMET -> "头盔";
            case ACCESSORY -> "饰品";
            case SKILL_STONE -> "技能石";
            case LEGS -> "护腿";
        };
    }
}
