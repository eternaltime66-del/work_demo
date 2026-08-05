package org.wx.core.wxBusiness.game.unit;

/**
 * 掉落数量非线性随机：数量越大权重越低（指数衰减）
 */
public final class DropRandomUnit {

    private DropRandomUnit() {
    }

    /**
     * 在 [min, max] 上按指数权重抽数量，偏小值
     */
    public static int randomDrop(int min, int max) {
        if (min > max) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        if (min == max) {
            return min;
        }

        double k = 0.5;
        double total = 0;
        double[] weights = new double[max - min + 1];

        for (int i = min; i <= max; i++) {
            double w = Math.exp(-k * (i - min));
            weights[i - min] = w;
            total += w;
        }

        double r = Math.random() * total;
        double sum = 0;

        for (int i = min; i <= max; i++) {
            sum += weights[i - min];
            if (r <= sum) {
                return i;
            }
        }

        return max;
    }
}
