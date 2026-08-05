package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 公式比较运算符
 */
public enum CompareOp {
    GT,
    GTE,
    LT,
    LTE,
    EQ;

    public String label() {
        return switch (this) {
            case GT -> "大于";
            case GTE -> "大于等于";
            case LT -> "小于";
            case LTE -> "小于等于";
            case EQ -> "等于";
        };
    }

    public String symbol() {
        return switch (this) {
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            case EQ -> "=";
        };
    }
}
