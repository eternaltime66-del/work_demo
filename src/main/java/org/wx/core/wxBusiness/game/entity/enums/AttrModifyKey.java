package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 属性修改：修改哪一项战斗属性（含战斗外被动同名的高级键）。
 */
public enum AttrModifyKey {
    ATK("攻击", true, ModKind.FLAT),
    MAX_HP("最大生命", true, ModKind.FLAT),
    DEF("防御", true, ModKind.FLAT),

    LIFE_STEAL("吸血", false, ModKind.ADD_PERCENT),
    ATK_SPEED("攻速", false, ModKind.ADD_PERCENT),
    DEAL_DMG_RATIO("造成伤害比例", false, ModKind.MULT_PERCENT),
    TAKEN_DMG_RATIO("受到伤害比例", false, ModKind.MULT_PERCENT),
    FINAL_ATK("最终攻击", false, ModKind.ADD_PERCENT),
    FINAL_HP("最终生命", false, ModKind.ADD_PERCENT),
    FINAL_DEF("最终防御", false, ModKind.ADD_PERCENT);

    public enum ModKind {
        FLAT,
        ADD_PERCENT,
        MULT_PERCENT
    }

    private final String label;
    private final boolean basic;
    private final ModKind modKind;

    AttrModifyKey(String label, boolean basic, ModKind modKind) {
        this.label = label;
        this.basic = basic;
        this.modKind = modKind;
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

    public ModKind getModKind() {
        return modKind;
    }
}
