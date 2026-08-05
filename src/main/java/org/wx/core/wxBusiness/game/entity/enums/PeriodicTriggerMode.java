package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 周期计算被动-触发模式
 */
public enum PeriodicTriggerMode {
    /** 自己数值触发 */
    SELF("自己数值触发"),
    /** 任意目标数值触发 */
    ANY("任意目标数值触发"),
    /** 敌方任意目标数值触发 */
    ANY_ENEMY("敌方任意目标数值触发"),
    /** 己方任意目标数值触发 */
    ANY_ALLY("己方任意目标数值触发"),
    /** 默认通用公式条件触发 */
    FORMULA("默认通用公式条件触发");

    private final String label;

    PeriodicTriggerMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 效果/公式是否可用「特定目标」（任意/敌方/己方模式） */
    public boolean hasSpecificTarget() {
        return this == ANY || this == ANY_ENEMY || this == ANY_ALLY;
    }
}
