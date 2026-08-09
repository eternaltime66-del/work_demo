package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 所需充能取值方式
 */
public enum NeedChargeMode {
    /** 取施法者自己的基础行动值 */
    SELF_BASE_ACTION("自己的基础行动值"),
    /** 后台手动填写数值 */
    MANUAL("手动输入"),
    /** 公式计算阈值 */
    FORMULA("公式读取");

    private final String label;

    NeedChargeMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
