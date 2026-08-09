package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 被动技能分类（技能 V2）：
 * <ul>
 *   <li>基础型：简单数值固化（攻防血 / 高级比例）</li>
 *   <li>战斗型：锚点 + 规则 + 动态输出</li>
 * </ul>
 */
public enum PassiveSkillType {
    /** 基础型 · 基础属性（平坦攻/血/防） */
    OUT_BASIC,
    /** 基础型 · 高级属性（吸血/攻速/伤害比例/最终比例等） */
    OUT_ADVANCED,
    /** 战斗型 · 开战锚点 */
    BATTLE_START,
    /** 战斗型 · 判定锚点 */
    BATTLE_JUDGE,
    /** 战斗型 · 脉冲锚点 */
    BATTLE_PULSE,
    /** 战斗型 · 战斗事件 */
    BATTLE_COMBAT,

    /** @deprecated V2 废弃，仅兼容旧数据 */
    @Deprecated
    IN_ANCHOR,
    /** @deprecated V2 废弃 */
    @Deprecated
    IN_PERIODIC,
    /** @deprecated V2 废弃 */
    @Deprecated
    IN_SUSTAINED;

    public String label() {
        return switch (this) {
            case OUT_BASIC -> "基础属性型·基础属性";
            case OUT_ADVANCED -> "基础属性型·高级属性";
            case BATTLE_START -> "战斗锚点型·开战锚点";
            case BATTLE_JUDGE -> "战斗锚点型·判定锚点";
            case BATTLE_PULSE -> "战斗锚点型·脉冲锚点";
            case BATTLE_COMBAT -> "战斗锚点型·战斗事件";
            case IN_ANCHOR -> "锚点被动(旧)";
            case IN_PERIODIC -> "周期被动(旧)";
            case IN_SUSTAINED -> "持续效果(旧)";
        };
    }

    /** 列表/摘要短标签 */
    public String shortLabel() {
        return switch (this) {
            case OUT_BASIC -> "基础属性";
            case OUT_ADVANCED -> "高级属性";
            case BATTLE_START -> "战斗开始后";
            case BATTLE_JUDGE -> "判定锚点";
            case BATTLE_PULSE -> "脉冲锚点";
            case BATTLE_COMBAT -> "战斗事件";
            case IN_ANCHOR -> "锚点被动";
            case IN_PERIODIC -> "周期被动";
            case IN_SUSTAINED -> "持续效果";
        };
    }

    public boolean isOutOfCombat() {
        return this == OUT_BASIC || this == OUT_ADVANCED;
    }

    /** V2 战斗型被动（引擎挂载） */
    public boolean isBattlePassive() {
        return this == BATTLE_START || this == BATTLE_JUDGE
                || this == BATTLE_PULSE || this == BATTLE_COMBAT;
    }

    public boolean isInCombat() {
        return isBattlePassive() || this == IN_ANCHOR || this == IN_PERIODIC || this == IN_SUSTAINED;
    }

    public boolean usesPeriodicFormula() {
        return this == BATTLE_JUDGE || this == BATTLE_PULSE
                || this == IN_PERIODIC || this == IN_SUSTAINED;
    }

    public boolean isLegacyCombat() {
        return this == IN_ANCHOR || this == IN_PERIODIC || this == IN_SUSTAINED;
    }
}
