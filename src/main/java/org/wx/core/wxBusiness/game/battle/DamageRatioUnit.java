package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

import java.math.BigDecimal;

/**
 * 伤害比例（乘法叠乘）：
 * <pre>
 * 防御后基础 = 公式伤害 × 攻击/(攻击+防御)
 * 最终 = 防御后基础
 *   × 造成伤害比例 × 受到伤害比例
 *   × (物理 ? 造成/受到物理伤害比例 : 造成/受到元素伤害比例)
 *   × 元素专属加成/抗性（ElementDamageUnit）
 * </pre>
 * 默认各比例 100%（乘数 1）。每次 +p% 按当前值 × (1+p/100) 叠乘。
 */
public final class DamageRatioUnit {

    private DamageRatioUnit() {
    }

    /**
     * 防御力减伤比例（承伤透过率）= 攻击方攻击 ÷ (攻击方攻击 + 防御方防御)。
     * 防御后基础伤害 = max(1, round(公式伤害 × 该比例))；公式伤害 ≤0 时返回 0。
     */
    public static int afterDefense(int amount, BattleRuntimeUnit dealer, BattleRuntimeUnit target) {
        if (amount <= 0) {
            return 0;
        }
        int atk = dealer == null ? 0 : Math.max(0, dealer.getAtk());
        int def = target == null ? 0 : Math.max(0, target.getDef());
        int sum = atk + def;
        if (sum <= 0) {
            return Math.max(1, amount);
        }
        return Math.max(1, (int) Math.round(amount * (atk / (double) sum)));
    }

    public static double ratioToMult(BigDecimal ratioPercent) {
        if (ratioPercent == null) {
            return 1D;
        }
        double v = ratioPercent.doubleValue() / 100D;
        return v <= 0D ? 0.0001D : v;
    }

    public static double factorFromSignedPercent(int signedPercent) {
        double f = 1D + signedPercent / 100D;
        return f <= 0D ? 0.0001D : f;
    }

    public static void applyMultFactor(BattleRuntimeUnit u, AttrModifyKey key, double factor) {
        if (u == null || key == null || factor == 0D) {
            return;
        }
        switch (key) {
            case DEAL_DMG_RATIO -> u.setDealDmgMult(safe(u.getDealDmgMult() * factor));
            case TAKEN_DMG_RATIO -> u.setTakenDmgMult(safe(u.getTakenDmgMult() * factor));
            case DEAL_ELEMENT_DMG_RATIO -> u.setDealElementDmgMult(safe(u.getDealElementDmgMult() * factor));
            case TAKEN_ELEMENT_DMG_RATIO -> u.setTakenElementDmgMult(safe(u.getTakenElementDmgMult() * factor));
            case DEAL_PHYS_DMG_RATIO -> u.setDealPhysDmgMult(safe(u.getDealPhysDmgMult() * factor));
            case TAKEN_PHYS_DMG_RATIO -> u.setTakenPhysDmgMult(safe(u.getTakenPhysDmgMult() * factor));
            default -> {
            }
        }
    }

    public static void revokeMultFactor(BattleRuntimeUnit u, AttrModifyKey key, double factor) {
        if (u == null || key == null || factor == 0D) {
            return;
        }
        applyMultFactor(u, key, 1D / factor);
    }

    public static boolean isDamageRatioKey(AttrModifyKey key) {
        return key == AttrModifyKey.DEAL_DMG_RATIO
                || key == AttrModifyKey.TAKEN_DMG_RATIO
                || key == AttrModifyKey.DEAL_ELEMENT_DMG_RATIO
                || key == AttrModifyKey.TAKEN_ELEMENT_DMG_RATIO
                || key == AttrModifyKey.DEAL_PHYS_DMG_RATIO
                || key == AttrModifyKey.TAKEN_PHYS_DMG_RATIO;
    }

    /**
     * 结算最终伤害（含通用/物理或元素比例 + 元素专属加成抗性）。
     */
    public static int finalizeDealt(
            int amount,
            BattleRuntimeUnit dealer,
            BattleRuntimeUnit target,
            DamageElement element
    ) {
        if (amount <= 0) {
            return 0;
        }
        DamageElement el = element == null ? DamageElement.PHYSICAL : element;
        double deal = dealer == null ? 1D : Math.max(0D, dealer.getDealDmgMult());
        double taken = target == null ? 1D : Math.max(0D, target.getTakenDmgMult());
        double typeDeal;
        double typeTaken;
        if (el == DamageElement.PHYSICAL) {
            typeDeal = dealer == null ? 1D : Math.max(0D, dealer.getDealPhysDmgMult());
            typeTaken = target == null ? 1D : Math.max(0D, target.getTakenPhysDmgMult());
        } else {
            typeDeal = dealer == null ? 1D : Math.max(0D, dealer.getDealElementDmgMult());
            typeTaken = target == null ? 1D : Math.max(0D, target.getTakenElementDmgMult());
        }
        int afterRatio = Math.max(1, (int) Math.round(amount * deal * taken * typeDeal * typeTaken));
        return ElementDamageUnit.finalizeDamage(afterRatio, dealer, target, el);
    }

    private static double safe(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0D) {
            return 0.0001D;
        }
        return v;
    }
}
