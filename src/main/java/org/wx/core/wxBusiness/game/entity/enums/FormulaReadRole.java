package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 公式「读取」参数的角色来源。
 */
public enum FormulaReadRole {
    /** 持有者 / 施法者自己 */
    SELF("自己"),
    /** 全局（如已经过行动值） */
    GLOBAL("全局"),
    /** 全部敌方（相对自己，属性/统计求和） */
    ALL_ENEMY("全部敌方"),
    /** 全部己方（相对自己，属性/统计求和） */
    ALL_ALLY("全部己方"),
    /** 结算时的每一个目标（技能效果） */
    EACH_TARGET("每个目标"),
    /** 锚点：每个受击目标 */
    ANCHOR_HIT_TARGET("每个受击目标"),
    /** 锚点：施法目标（施法者） */
    ANCHOR_CASTER("施法目标"),
    /** 锚点：伤害来源目标 */
    DAMAGE_SOURCE("伤害来源目标"),
    /** 锚点：每一个被伤害的目标 */
    EACH_DAMAGED_TARGET("每一个被伤害的目标"),
    /** 周期：特定目标 */
    SPECIFIC_TARGET("特定目标");

    private final String label;

    FormulaReadRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isUnitRole() {
        return this == SELF
                || this == EACH_TARGET
                || this == ANCHOR_HIT_TARGET
                || this == ANCHOR_CASTER
                || this == DAMAGE_SOURCE
                || this == EACH_DAMAGED_TARGET
                || this == SPECIFIC_TARGET;
    }

    public boolean isAggregateRole() {
        return this == ALL_ENEMY || this == ALL_ALLY;
    }
}
