package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 全局读取插槽键（公式 ATTR / 统计）。
 */
public enum FormulaReadKey {
    MAX_HP("最大生命"),
    HP("当前生命"),
    ATK("攻击"),
    DEF("防御"),
    ACTION("角色行动值"),
    ELAPSED_ACTION("已经过行动值"),

    DEAL_DMG_AMOUNT("累计造成伤害量"),
    DEAL_HEAL_AMOUNT("累计造成治疗量"),
    TAKE_DMG_AMOUNT("累计受到伤害量"),
    TAKE_HEAL_AMOUNT("累计受到治疗量"),
    DEAL_DMG_COUNT("累计造成伤害次数"),
    DEAL_HEAL_COUNT("累计造成治疗次数"),
    TAKE_DMG_COUNT("累计受到伤害次数"),
    TAKE_HEAL_COUNT("累计受到治疗次数"),

    ELEMENT_DMG_BONUS("全局元素伤害加成"),
    ELEMENT_PHYS_BONUS("物理伤害加成"),
    ELEMENT_POISON_BONUS("中毒伤害加成"),
    ELEMENT_IGNITE_BONUS("点燃伤害加成"),
    ELEMENT_FREEZE_BONUS("冰冻伤害加成"),
    ELEMENT_SHOCK_BONUS("电击伤害加成"),
    ELEMENT_BURN_BONUS("灼烧伤害加成");

    private final String label;

    FormulaReadKey(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
