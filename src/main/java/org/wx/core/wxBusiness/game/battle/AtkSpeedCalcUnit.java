package org.wx.core.wxBusiness.game.battle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * 攻速 → 行动值（反比）：
 * <ul>
 *   <li>展示攻速 = 100 / 行动值（默认行动值 100 → 攻速 1）</li>
 *   <li>配置时以攻速为准：行动值 = round(100 / 攻速)</li>
 *   <li>装备增减先叠乘在攻速上，再反推行动值</li>
 * </ul>
 * 比例单位均为 1%。
 */
public final class AtkSpeedCalcUnit {

    /** 行动值与攻速的换算常数：100 行动值 = 1 攻速 */
    public static final double ACTION_ATK_SPEED_BASE = 100D;

    private AtkSpeedCalcUnit() {
    }

    /** 攻速 → 行动值 */
    public static int actionFromAtkSpeed(Number atkSpeed) {
        double s = normalizeAtkSpeed(atkSpeed);
        return Math.max(1, (int) Math.round(ACTION_ATK_SPEED_BASE / s));
    }

    /** 行动值 → 攻速 */
    public static BigDecimal atkSpeedFromAction(Integer action) {
        double a = action == null || action <= 0 ? ACTION_ATK_SPEED_BASE : action.doubleValue();
        return BigDecimal.valueOf(ACTION_ATK_SPEED_BASE / a).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal normalizeAtkSpeedBd(Number atkSpeed) {
        return BigDecimal.valueOf(normalizeAtkSpeed(atkSpeed)).setScale(4, RoundingMode.HALF_UP);
    }

    public static int calcFinalAction(Integer baseAction,
                                      Collection<? extends Number> upPercents,
                                      Collection<? extends Number> downPercents) {
        BigDecimal finalSpeed = calcFinalAtkSpeed(baseAction, upPercents, downPercents);
        return actionFromAtkSpeed(finalSpeed);
    }

    public static BigDecimal calcFinalAtkSpeed(Integer baseAction,
                                              Collection<? extends Number> upPercents,
                                              Collection<? extends Number> downPercents) {
        double action = baseAction == null || baseAction <= 0 ? ACTION_ATK_SPEED_BASE : baseAction.doubleValue();
        double atkSpeed = ACTION_ATK_SPEED_BASE / action;

        if (upPercents != null) {
            for (Number n : upPercents) {
                double r = toPercent(n);
                if (r == 0D) {
                    continue;
                }
                atkSpeed *= (1D + r / 100D);
            }
        }
        if (downPercents != null) {
            for (Number n : downPercents) {
                double r = toPercent(n);
                if (r == 0D) {
                    continue;
                }
                atkSpeed *= Math.max(0D, 1D - r / 100D);
            }
        }
        if (atkSpeed <= 0D) {
            return BigDecimal.valueOf(0.0001);
        }
        return BigDecimal.valueOf(atkSpeed).setScale(4, RoundingMode.HALF_UP);
    }

    private static double normalizeAtkSpeed(Number atkSpeed) {
        if (atkSpeed == null) {
            return 1D;
        }
        double s = atkSpeed instanceof BigDecimal bd ? bd.doubleValue() : atkSpeed.doubleValue();
        if (s <= 0D) {
            return 1D;
        }
        return s;
    }

    private static double toPercent(Number n) {
        if (n == null) {
            return 0D;
        }
        if (n instanceof BigDecimal bd) {
            return bd.doubleValue();
        }
        return n.doubleValue();
    }
}
