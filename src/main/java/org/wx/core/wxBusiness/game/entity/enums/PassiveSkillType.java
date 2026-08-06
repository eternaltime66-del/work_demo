package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 被动技能分类
 */
public enum PassiveSkillType {
    /** 战斗外-基础属性被动 */
    OUT_BASIC,
    /** 战斗外-高级属性被动（含吸血/攻速/伤害比例/最终攻防血比例等） */
    OUT_ADVANCED,
    /** 战斗内-锚点被动 */
    IN_ANCHOR,
    /** 战斗内-周期计算被动 */
    IN_PERIODIC,
    /** 战斗内-持续效果（条件成立生效，不成立取消） */
    IN_SUSTAINED;

    public String label() {
        return switch (this) {
            case OUT_BASIC -> "基础属性被动";
            case OUT_ADVANCED -> "高级属性被动";
            case IN_ANCHOR -> "锚点被动";
            case IN_PERIODIC -> "周期计算被动";
            case IN_SUSTAINED -> "持续效果";
        };
    }

    public boolean isOutOfCombat() {
        return this == OUT_BASIC || this == OUT_ADVANCED;
    }

    public boolean isInCombat() {
        return this == IN_ANCHOR || this == IN_PERIODIC || this == IN_SUSTAINED;
    }

    /** 使用周期公式字段做条件判定（周期 / 持续效果） */
    public boolean usesPeriodicFormula() {
        return this == IN_PERIODIC || this == IN_SUSTAINED;
    }
}
