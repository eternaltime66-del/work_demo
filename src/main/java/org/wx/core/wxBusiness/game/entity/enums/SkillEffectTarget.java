package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 技能效果目标。
 * <p>
 * 布阵 6×5（列 col 0..5 从左到右，行 row 0..4）：
 * <ul>
 *   <li>怪物侧：最下一行(row 最大)为前排，最上一行(row 最小)为后排</li>
 *   <li>己方：最上一行(row 最小)为前排，最下一行(row 最大)为后排</li>
 *   <li>首目标：从左到右扫；当前行无可用目标则换下一排继续找
 *       （敌方从前排往身后扫，己方从前排往身后扫）</li>
 * </ul>
 */
public enum SkillEffectTarget {
    SELF("自己"),
    FIRST("首目标"),
    FRONT_ROW("前排"),
    BACK_ROW("后排"),
    RANDOM_ENEMY("随机一个敌方"),
    ENEMY_MAX_HP("生命值最高敌方"),
    ENEMY_MIN_HP("生命值最低敌方"),
    ALLY_MIN_HP("生命值最低己方"),
    ALL_ENEMY("敌方全部"),
    ALL_ALLY("己方全部");

    private final String label;

    SkillEffectTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
