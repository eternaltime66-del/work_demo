package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

/**
 * 元素伤害结算：base × (1+专属加成) × 抗性减免。
 */
public final class ElementDamageUnit {

    private ElementDamageUnit() {
    }

    public static int finalizeDamage(int base, BattleRuntimeUnit dealer, BattleRuntimeUnit target, DamageElement element) {
        if (base <= 0) {
            return 0;
        }
        DamageElement el = element == null ? DamageElement.PHYSICAL : element;
        double bonusPct = elementBonus(dealer, el) + (dealer == null ? 0 : dealer.getElementDmgBonus());
        double resistPct = elementResist(target, el);
        double mult = (1D + bonusPct / 100D) * Math.max(0D, 1D - resistPct / 100D);
        return Math.max(1, (int) Math.round(base * mult));
    }

    public static double elementBonus(BattleRuntimeUnit u, DamageElement el) {
        if (u == null || el == null) {
            return 0;
        }
        return switch (el) {
            case PHYSICAL -> u.getElementPhysBonus();
            case POISON -> u.getElementPoisonBonus();
            case IGNITE -> u.getElementIgniteBonus();
            case FREEZE -> u.getElementFreezeBonus();
            case SHOCK -> u.getElementShockBonus();
            case BURN -> u.getElementBurnBonus();
        };
    }

    public static double elementResist(BattleRuntimeUnit u, DamageElement el) {
        if (u == null || el == null) {
            return 0;
        }
        return switch (el) {
            case PHYSICAL -> u.getResistPhys();
            case POISON -> u.getResistPoison();
            case IGNITE -> u.getResistIgnite();
            case FREEZE -> u.getResistFreeze();
            case SHOCK -> u.getResistShock();
            case BURN -> u.getResistBurn();
        };
    }
}
