package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 公式编辑器「读取」属性键（对应战斗统计；可继续扩展）。
 * 效果公式还需搭配 {@link FormulaReadRole}：自己 / 每个目标。
 */
public enum FormulaReadKey {
    MAX_HP("最大生命"),
    HP("当前生命"),
    ATK("攻击力"),
    DEF("防御力"),
    ACTION("行动值"),
    ELAPSED_ACTION("已经过行动值");

    private final String label;

    FormulaReadKey(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
