package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeScope;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

/**
 * 根据战斗事件结算充能：行动值推进 / 技能释放 / 受到技能
 */
public final class ChargeAccrualUnit {

    private ChargeAccrualUnit() {
    }

    /**
     * 全局：每经过 x 行动值增加 y 点充能。
     * 用「已经过行动值」整除计算累计应得充能（战斗演算侧可再做差分）。
     *
     * @return 按当前已经过行动值应得的总充能点数；不匹配条件时返回 0
     */
    public static int totalChargeFromElapsed(SkillCharge charge, int elapsedActionValue) {
        if (charge == null || charge.getConditionType() != ChargeConditionType.ACTION_VALUE) {
            return 0;
        }
        ChargeScope scope = charge.getScope() == null ? ChargeScope.GLOBAL : charge.getScope();
        if (scope != ChargeScope.GLOBAL) {
            return 0;
        }
        int every = charge.getEveryActionValue() == null ? 0 : charge.getEveryActionValue();
        int gain = charge.getChargeGain() == null ? 0 : charge.getChargeGain();
        if (every <= 0 || gain == 0 || elapsedActionValue <= 0) {
            return 0;
        }
        return (elapsedActionValue / every) * gain;
    }

    /**
     * 行动值推进 delta 时，本次新增充能（相对推进前的差分）
     */
    public static int chargeGainedOnAdvance(SkillCharge charge, int elapsedBefore, int delta) {
        if (delta <= 0) {
            return 0;
        }
        int before = totalChargeFromElapsed(charge, elapsedBefore);
        int after = totalChargeFromElapsed(charge, elapsedBefore + delta);
        return Math.max(0, after - before);
    }

    /**
     * 技能充能条件：释放 / 受到 / 造成伤害 / 受到伤害 / 造成击杀。
     *
     * @param event   事件类型
     * @param trigger 触发匹配的技能
     */
    public static int chargeGainedOnSkillEvent(SkillCharge charge, SkillChargeEvent event, ActiveSkill trigger) {
        if (charge == null || charge.getConditionType() != ChargeConditionType.SKILL_CHARGE) {
            return 0;
        }
        if (event == null || charge.getSkillChargeEvent() != event) {
            return 0;
        }
        if (trigger == null || Wx.isEmpty(trigger.getId())) {
            return 0;
        }
        if (!matchesSkill(charge, trigger)) {
            return 0;
        }
        int gain = charge.getChargeGain() == null ? 0 : charge.getChargeGain();
        return Math.max(0, gain);
    }

    private static boolean matchesSkill(SkillCharge charge, ActiveSkill trigger) {
        SkillChargeMatchMode mode = charge.getSkillChargeMatch() == null
                ? SkillChargeMatchMode.ANY
                : charge.getSkillChargeMatch();
        return switch (mode) {
            case ANY -> true;
            case ANY_TYPE -> {
                ActiveSkillType need = charge.getMatchSkillType();
                yield need != null && need == trigger.getSkillType();
            }
            case ANY_SCHOOL -> SkillSchoolUnit.schoolEquals(charge.getMatchSkillSchool(), SkillSchoolUnit.schoolOf(trigger));
            case ANY_ELEMENT -> SkillSchoolUnit.elementEquals(charge.getMatchDamageElement(), SkillSchoolUnit.elementOf(trigger));
            case SPECIFIC -> !Wx.isEmpty(charge.getMatchSkillId())
                    && charge.getMatchSkillId().equals(trigger.getId());
        };
    }
}
