package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 公式编辑器「读取」- 属性类键（readCategory=ATTR）及全局键。
 * <ul>
 *   <li>自己 / 每个目标：MAX_HP / HP(战斗内) / ATK / DEF / ACTION(角色行动值)</li>
 *   <li>全局：ELAPSED_ACTION</li>
 * </ul>
 */
public enum FormulaReadKey {
    MAX_HP("最大生命"),
    /** 当前生命（战斗内才有意义） */
    HP("当前生命"),
    ATK("攻击"),
    DEF("防御"),
    /** 角色行动值（非战斗内当前进度） */
    ACTION("角色行动值"),
    /** 全局已经过行动值 */
    ELAPSED_ACTION("已经过行动值");

    private final String label;

    FormulaReadKey(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
