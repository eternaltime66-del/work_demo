package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 技能 / 锚点被动效果目标。
 * <p>
 * 布阵 6×5（列 col 0..5 从左到右，行 row 0..4）：
 * <ul>
 *   <li>怪物侧：最下一行(row 最大)为前排，最上一行(row 最小)为后排</li>
 *   <li>己方：最上一行(row 最小)为前排，最下一行(row 最大)为后排</li>
 *   <li>首目标：从左到右扫；当前行无可用目标则换下一排继续找
 *       （敌方从前排往身后扫，己方从前排往身后扫）</li>
 *   <li>锚点专属目标仅锚点被动效果可用，需配合 AnchorEvalContext</li>
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
    ALL_ALLY("己方全部"),

    /** 锚点：每个受击目标（释放充能技能后） */
    ANCHOR_HIT_TARGET("每个受击目标"),
    /** 锚点：施法目标/施法者（受到充能技能后） */
    ANCHOR_CASTER("施法目标"),
    /** 锚点：伤害来源（受到充能技能伤害后） */
    DAMAGE_SOURCE("伤害来源目标"),
    /** 锚点：每一个被伤害的目标（造成充能技能伤害后） */
    EACH_DAMAGED_TARGET("每一个被伤害的目标"),
    /** 周期：特定目标（触发扫描命中的候选单位） */
    SPECIFIC_TARGET("特定目标");

    private final String label;

    SkillEffectTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 是否为锚点被动专属目标（主动技能不可选） */
    public boolean isAnchorExclusive() {
        return this == ANCHOR_HIT_TARGET
                || this == ANCHOR_CASTER
                || this == DAMAGE_SOURCE
                || this == EACH_DAMAGED_TARGET;
    }

    public boolean isPeriodicExclusive() {
        return this == SPECIFIC_TARGET;
    }
}
