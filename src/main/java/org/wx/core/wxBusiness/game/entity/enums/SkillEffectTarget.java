package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 通用角色选择模板（技能 V2）。
 * <p>
 * 首位：前排起每排从左到右。敌方前排=row 最大；己方前排=row 最小。
 */
public enum SkillEffectTarget {
    SELF("自身"),

    // —— 己方单体 ——
    ALLY_FIRST("战场首位己方"),
    RANDOM_ALLY("随机己方单体"),
    ALLY_MAX_HP("当前生命最高己方"),
    ALLY_MIN_HP("当前生命最低己方"),
    ALLY_MAX_MAX_HP("最大生命最高己方"),
    ALLY_MIN_MAX_HP("最大生命最低己方"),
    ALLY_MAX_HP_PCT("生命百分比最高己方"),
    ALLY_MIN_HP_PCT("生命百分比最低己方"),
    ALLY_MAX_ATK("攻击最高己方"),
    ALLY_MIN_ATK("攻击最低己方"),

    // —— 己方群体 ——
    ALL_ALLY("全部己方"),
    ALLY_FRONT_ROW("己方第一排"),
    ALLY_BACK_ROW("己方最后一排"),

    // —— 敌方单体（兼容旧名）——
    FIRST("战场首位敌方"),
    RANDOM_ENEMY("随机敌方单体"),
    ENEMY_MAX_HP("当前生命最高敌方"),
    ENEMY_MIN_HP("当前生命最低敌方"),
    ENEMY_MAX_MAX_HP("最大生命最高敌方"),
    ENEMY_MIN_MAX_HP("最大生命最低敌方"),
    ENEMY_MAX_HP_PCT("生命百分比最高敌方"),
    ENEMY_MIN_HP_PCT("生命百分比最低敌方"),
    ENEMY_MAX_ATK("攻击最高敌方"),
    ENEMY_MIN_ATK("攻击最低敌方"),

    // —— 敌方群体 ——
    ALL_ENEMY("全部敌方"),
    FRONT_ROW("敌方第一排"),
    BACK_ROW("敌方最后一排"),

    // —— 全场 ——
    FIELD_MAX_HP("全场当前生命最高"),
    FIELD_MIN_HP("全场当前生命最低"),
    FIELD_MAX_MAX_HP("全场最大生命最高"),
    FIELD_MIN_MAX_HP("全场最大生命最低"),
    FIELD_MAX_HP_PCT("全场生命百分比最高"),
    FIELD_MIN_HP_PCT("全场生命百分比最低"),
    FIELD_MAX_ATK("全场攻击最高"),
    FIELD_MIN_ATK("全场攻击最低"),
    ALL_FIELD("全场所有存活"),
    FIELD_FRONT_ROW("全场第一排"),
    FIELD_BACK_ROW("全场最后一排"),

    /** 特定情况占位 */
    SPECIAL("特定情况"),

    // —— 战斗锚点专属 ——
    EVENT_HIT_TARGETS("本次受击目标"),
    EVENT_CASTER("本次施法者"),
    EVENT_DAMAGE_SOURCE("本次伤害来源"),
    EVENT_PULSE_CASTER("脉冲BUFF施法者"),
    EVENT_KILLER("击杀方"),
    EVENT_KILLED("被击杀方"),
    SPECIFIC_TARGET("特定目标"),

    /** @deprecated 兼容旧配置 */
    @Deprecated
    ANCHOR_HIT_TARGET("每个受击目标(旧)"),
    /** @deprecated */
    @Deprecated
    ANCHOR_CASTER("施法目标(旧)"),
    /** @deprecated */
    @Deprecated
    DAMAGE_SOURCE("伤害来源目标(旧)"),
    /** @deprecated */
    @Deprecated
    EACH_DAMAGED_TARGET("每一个被伤害的目标(旧)");

    private final String label;

    SkillEffectTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEventExclusive() {
        return this == EVENT_HIT_TARGETS
                || this == EVENT_CASTER
                || this == EVENT_DAMAGE_SOURCE
                || this == EVENT_PULSE_CASTER
                || this == EVENT_KILLER
                || this == EVENT_KILLED
                || this == SPECIFIC_TARGET
                || isAnchorExclusive();
    }

    /** @deprecated use {@link #isEventExclusive()} */
    @Deprecated
    public boolean isAnchorExclusive() {
        return this == ANCHOR_HIT_TARGET
                || this == ANCHOR_CASTER
                || this == DAMAGE_SOURCE
                || this == EACH_DAMAGED_TARGET
                || this == EVENT_HIT_TARGETS
                || this == EVENT_CASTER
                || this == EVENT_DAMAGE_SOURCE
                || this == EVENT_PULSE_CASTER
                || this == EVENT_KILLER
                || this == EVENT_KILLED;
    }

    public boolean isPeriodicExclusive() {
        return this == SPECIFIC_TARGET;
    }
}
