package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 基础型被动-效果属性。
 * <ul>
 *   <li>基础属性：攻击/生命/防御，平坦加减</li>
 *   <li>高级属性：吸血、攻速、最终攻防血比例为加法；伤害比例类为先后顺序乘法叠乘</li>
 * </ul>
 */
public enum PassiveEffectAttrKey {
    ATK("攻击", true, StackMode.ADD_FLAT),
    MAX_HP("生命", true, StackMode.ADD_FLAT),
    DEF("防御", true, StackMode.ADD_FLAT),

    LIFE_STEAL("吸血", false, StackMode.ADD_PERCENT),
    ATK_SPEED("攻速", false, StackMode.ADD_PERCENT),
    DEAL_DMG_RATIO("造成伤害比例", false, StackMode.MULT_PERCENT),
    TAKEN_DMG_RATIO("受到伤害比例", false, StackMode.MULT_PERCENT),
    DEAL_ELEMENT_DMG_RATIO("造成元素伤害比例", false, StackMode.MULT_PERCENT),
    TAKEN_ELEMENT_DMG_RATIO("受到元素伤害比例", false, StackMode.MULT_PERCENT),
    DEAL_PHYS_DMG_RATIO("造成物理伤害比例", false, StackMode.MULT_PERCENT),
    TAKEN_PHYS_DMG_RATIO("受到物理伤害比例", false, StackMode.MULT_PERCENT),
    FINAL_ATK("最终攻击", false, StackMode.ADD_PERCENT),
    FINAL_HP("最终生命", false, StackMode.ADD_PERCENT),
    FINAL_DEF("最终防御", false, StackMode.ADD_PERCENT);

    public enum StackMode {
        ADD_FLAT,
        ADD_PERCENT,
        MULT_PERCENT
    }

    private final String label;
    private final boolean basic;
    private final StackMode stackMode;

    PassiveEffectAttrKey(String label, boolean basic, StackMode stackMode) {
        this.label = label;
        this.basic = basic;
        this.stackMode = stackMode;
    }

    public String getLabel() {
        return label;
    }

    public boolean isBasic() {
        return basic;
    }

    public boolean isAdvanced() {
        return !basic;
    }

    /** 最终攻击/生命/防御比例 */
    public boolean isFinalRatio() {
        return this == FINAL_ATK || this == FINAL_HP || this == FINAL_DEF;
    }

    public boolean isDamageRatio() {
        return this == DEAL_DMG_RATIO || this == TAKEN_DMG_RATIO
                || this == DEAL_ELEMENT_DMG_RATIO || this == TAKEN_ELEMENT_DMG_RATIO
                || this == DEAL_PHYS_DMG_RATIO || this == TAKEN_PHYS_DMG_RATIO;
    }

    public StackMode getStackMode() {
        return stackMode;
    }
}
