package org.wx.core.wxBusiness.game.battle;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 最终攻击/生命/防御：合计值 × 最终比例（单位 1%，默认 100）。
 */
public final class FinalStatCalcUnit {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MIN_RATIO = new BigDecimal("0.0001");

    private FinalStatCalcUnit() {
    }

    public static BigDecimal normalizeRatio(BigDecimal ratioPercent) {
        if (ratioPercent == null) {
            return HUNDRED;
        }
        if (ratioPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return MIN_RATIO;
        }
        return ratioPercent;
    }

    /** 合计 × 比例%，四舍五入；允许为 0 */
    public static int apply(int sum, BigDecimal ratioPercent) {
        BigDecimal r = normalizeRatio(ratioPercent);
        return BigDecimal.valueOf(sum)
                .multiply(r)
                .divide(HUNDRED, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** 生命至少 1 */
    public static int applyHp(int sum, BigDecimal ratioPercent) {
        return Math.max(1, apply(sum, ratioPercent));
    }
}
