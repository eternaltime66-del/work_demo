package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 战斗外被动-效果属性。
 * <ul>
 *   <li>基础：攻击/生命/防御，平坦加减</li>
 *   <li>高级：吸血、攻速、最终攻防血比例为加法；造成/受到伤害比例叠乘</li>
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

    public StackMode getStackMode() {
        return stackMode;
    }
}
