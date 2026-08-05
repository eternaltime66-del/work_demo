package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 属性修改：修改哪一项战斗属性
 */
public enum AttrModifyKey {
    ATK("攻击"),
    MAX_HP("最大生命"),
    DEF("防御");

    private final String label;

    AttrModifyKey(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
