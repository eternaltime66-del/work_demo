package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 属性修改：增加 / 减少
 */
public enum AttrModifyDirection {
    INCREASE("增加"),
    DECREASE("减少");

    private final String label;

    AttrModifyDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
