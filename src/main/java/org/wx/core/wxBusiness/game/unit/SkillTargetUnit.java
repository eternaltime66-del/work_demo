package org.wx.core.wxBusiness.game.unit;

/**
 * 技能目标解析约定（布阵 6×5）。
 * <p>
 * 行号约定：row 越小越靠上，越大越靠下。
 * <ul>
 *   <li>怪物：前排 = 最大 row（最下），后排 = 最小 row（最上）</li>
 *   <li>己方：前排 = 最小 row（最上），后排 = 最大 row（最下）</li>
 *   <li>首目标：按「前排 → 身后各排」顺序；每一排内从左到右(col 小→大)，
 *       该排有可用单位则取最左一个，否则继续下一排</li>
 * </ul>
 */
public final class SkillTargetUnit {

    private SkillTargetUnit() {
    }

    /** 怪物前排方向：row 越大越靠前 */
    public static boolean isMonsterFrontPreferLargerRow() {
        return true;
    }

    /** 己方前排方向：row 越小越靠前 */
    public static boolean isAllyFrontPreferSmallerRow() {
        return true;
    }

    /**
     * 前排判定：相对战场一侧的「最前一排」上的单位。
     * 具体实现待战斗单位列表接入后补齐。
     */
    public static int compareFrontness(boolean allySide, int rowA, int rowB) {
        if (allySide) {
            return Integer.compare(rowA, rowB); // 小 row 更靠前
        }
        return Integer.compare(rowB, rowA); // 大 row 更靠前
    }
}
