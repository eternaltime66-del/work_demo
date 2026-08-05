package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 物品类型
 */
public enum ItemType {
    /** 材料 */
    MATERIAL,
    /** 武器 */
    WEAPON,
    /** 护甲 */
    ARMOR,
    /** 护手 */
    GLOVES,
    /** 头盔 */
    HELMET,
    /** 饰品 */
    ACCESSORY,
    /** 护腿 */
    LEGS;

    public String label() {
        return switch (this) {
            case MATERIAL -> "材料";
            case WEAPON -> "武器";
            case ARMOR -> "护甲";
            case GLOVES -> "护手";
            case HELMET -> "头盔";
            case ACCESSORY -> "饰品";
            case LEGS -> "护腿";
        };
    }
}
