package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 伤害元素类型；未配置时默认物理。
 */
public enum DamageElement {
    PHYSICAL("物理"),
    POISON("中毒"),
    IGNITE("点燃"),
    FREEZE("冰冻"),
    SHOCK("电击"),
    BURN("灼烧");

    private final String label;

    DamageElement(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
