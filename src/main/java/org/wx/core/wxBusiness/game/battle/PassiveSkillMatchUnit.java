package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

/**
 * 锚点被动：触发技能是否匹配配置
 */
public final class PassiveSkillMatchUnit {

    private PassiveSkillMatchUnit() {
    }

    /**
     * 充能技能 = 全部主动技能（普攻 / 大招 / 小技能）。
     * 装备「充能技能槽」仅挂大招/小技能，与此判定无关。
     */
    public static boolean isChargeSkill(ActiveSkill skill) {
        return skill != null && skill.getSkillType() != null;
    }

    /** V2 战斗事件：按 skillMatchMode 过滤触发技能 */
    public static boolean matchesSkillRef(PassiveSkill passive, ActiveSkill trigger) {
        if (passive == null) {
            return false;
        }
        return matchesSkillRef(
                passive.getSkillMatchMode(),
                passive.getRefSkillType(),
                passive.getRefSkillSchool(),
                passive.getRefDamageElement(),
                passive.getRefSkillId(),
                trigger
        );
    }

    /** 通用五维技能匹配（充能 / 被动 / 闪避等） */
    public static boolean matchesSkillRef(
            SkillChargeMatchMode mode,
            ActiveSkillType refType,
            String refSchool,
            DamageElement refElement,
            String refSkillId,
            ActiveSkill trigger
    ) {
        if (trigger == null) {
            return true;
        }
        if (mode == null || mode == SkillChargeMatchMode.ANY) {
            return true;
        }
        return switch (mode) {
            case ANY -> true;
            case ANY_TYPE -> trigger.getSkillType() == refType;
            case ANY_SCHOOL -> SkillSchoolUnit.schoolEquals(refSchool, SkillSchoolUnit.schoolOf(trigger));
            case ANY_ELEMENT -> SkillSchoolUnit.elementEquals(refElement, SkillSchoolUnit.elementOf(trigger));
            case SPECIFIC -> trigger.getId() != null && trigger.getId().equals(refSkillId);
        };
    }

    public static String matchScopeLabel(
            SkillChargeMatchMode mode,
            ActiveSkillType refType,
            String refSchool,
            DamageElement refElement,
            String refSkillId
    ) {
        if (mode == null || mode == SkillChargeMatchMode.ANY) {
            return "任意技能";
        }
        return switch (mode) {
            case ANY -> "任意技能";
            case ANY_TYPE -> "类型「" + (refType != null ? refType.name() : "?") + "」";
            case ANY_SCHOOL -> "流派「" + SkillSchoolUnit.normalizeSchool(refSchool) + "」";
            case ANY_ELEMENT -> "元素「" + (refElement != null ? refElement.name() : "PHYSICAL") + "」";
            case SPECIFIC -> refSkillId != null && !refSkillId.isBlank()
                    ? ("指定技能")
                    : "指定技能";
        };
    }
}
