package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

/**
 * 技能流派 / 元素归一与比较。
 */
public final class SkillSchoolUnit {

    public static final String DEFAULT_SCHOOL = "无";

    private SkillSchoolUnit() {
    }

    public static String normalizeSchool(String school) {
        if (school == null) {
            return DEFAULT_SCHOOL;
        }
        String t = school.trim();
        return t.isEmpty() ? DEFAULT_SCHOOL : t;
    }

    public static boolean schoolEquals(String a, String b) {
        return normalizeSchool(a).equals(normalizeSchool(b));
    }

    public static DamageElement normalizeElement(DamageElement element) {
        return element != null ? element : DamageElement.PHYSICAL;
    }

    public static boolean elementEquals(DamageElement a, DamageElement b) {
        return normalizeElement(a) == normalizeElement(b);
    }

    public static String schoolOf(ActiveSkill skill) {
        return skill == null ? DEFAULT_SCHOOL : normalizeSchool(skill.getSkillSchool());
    }

    public static DamageElement elementOf(ActiveSkill skill) {
        return skill == null ? DamageElement.PHYSICAL : normalizeElement(skill.getDamageElement());
    }
}
