package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 基础锚点（开战）施加规则。
 */
public enum BattleStartApplyRule {
    /** 开战立即一次 */
    IMMEDIATE,
    /** 达到指定行动值后单次 */
    AT_ELAPSED_ONCE,
    /** 每达到指定行动值脉冲 */
    EVERY_ELAPSED
}
