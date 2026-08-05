package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 被动生效条件模式
 */
public enum PassiveConditionMode {
    /** 不限（始终可生效） */
    UNLIMITED,
    /** 选择条件（满足判定组） */
    SELECT;

    public String label() {
        return switch (this) {
            case UNLIMITED -> "不限";
            case SELECT -> "选择";
        };
    }
}
