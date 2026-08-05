package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 公式「读取」参数的角色来源（效果公式用）
 */
public enum FormulaReadRole {
    /** 施法者自己 */
    SELF("自己"),
    /** 结算时的每一个目标（多目标时按目标分别取属性） */
    EACH_TARGET("每个目标");

    private final String label;

    FormulaReadRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
