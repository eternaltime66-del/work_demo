package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 技能效果类型
 */
public enum SkillEffectType {
    /** 伤害（公式 + 段数） */
    DAMAGE("伤害"),
    /** 治疗（公式 + 段数，模板与伤害类似） */
    HEAL("治疗"),
    /** 属性修改（攻击/最大生命/防御 × 增加/减少 + 公式） */
    ATTR_MODIFY("属性修改");

    private final String label;

    SkillEffectType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
