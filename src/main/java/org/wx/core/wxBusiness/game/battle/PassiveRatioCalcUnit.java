package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 被动高级比例结算：
 * <ul>
 *   <li>吸血 / 攻速 / 最终攻击·生命·防御：加法（单位 1%）</li>
 *   <li>造成/受到伤害、元素伤害、物理伤害比例：每条独立叠乘 1±p%，最终严格 &gt; 0</li>
 * </ul>
 */
public final class PassiveRatioCalcUnit {

    private static final BigDecimal MIN_RATIO = new BigDecimal("0.0001");

    private PassiveRatioCalcUnit() {
    }

    /** 吸血/攻速：base + Σ(±value)，可为 0，不为负 */
    public static BigDecimal applyAdditivePercent(BigDecimal base, List<SignedPercent> deltas) {
        BigDecimal v = base == null ? BigDecimal.ZERO : base;
        if (deltas != null) {
            for (SignedPercent d : deltas) {
                if (d == null || d.value == null) {
                    continue;
                }
                v = v.add(d.signed());
            }
        }
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return v;
    }

    /**
     * 叠乘比例：从 base（通常 100 表示 100%）出发，每条 ×(1±p/100)。
     * 结果换算回「单位 1%」数值，且严格 &gt; 0。
     */
    public static BigDecimal applyMultiplicativePercent(BigDecimal basePercent, List<SignedPercent> deltas) {
        double factor = basePercent == null ? 1D : basePercent.doubleValue() / 100D;
        if (factor <= 0D) {
            factor = 1D;
        }
        if (deltas != null) {
            for (SignedPercent d : deltas) {
                if (d == null || d.value == null) {
                    continue;
                }
                double p = d.value.doubleValue();
                if (p == 0D) {
                    continue;
                }
                if (d.increase) {
                    factor *= (1D + p / 100D);
                } else {
                    factor *= (1D - p / 100D);
                }
            }
        }
        if (factor <= 0D) {
            return MIN_RATIO;
        }
        return BigDecimal.valueOf(factor * 100D).setScale(4, RoundingMode.HALF_UP).max(MIN_RATIO);
    }

    public static boolean isMultiplicative(PassiveEffectAttrKey key) {
        return key != null && key.getStackMode() == PassiveEffectAttrKey.StackMode.MULT_PERCENT;
    }

    public static SignedPercent of(AttrModifyDirection dir, BigDecimal value) {
        return new SignedPercent(dir != AttrModifyDirection.DECREASE, value);
    }

    public static final class SignedPercent {
        public final boolean increase;
        public final BigDecimal value;

        public SignedPercent(boolean increase, BigDecimal value) {
            this.increase = increase;
            this.value = value;
        }

        public BigDecimal signed() {
            if (value == null) {
                return BigDecimal.ZERO;
            }
            return increase ? value : value.negate();
        }
    }
}
