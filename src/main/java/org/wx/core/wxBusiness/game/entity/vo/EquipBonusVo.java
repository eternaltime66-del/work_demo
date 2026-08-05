package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 当前穿戴装备提供的战斗属性加成（含默认战斗外被动）。
 */
@Data
public class EquipBonusVo {

    private int atk;
    private int hp;
    private int defense;

    /** 最终攻击比例加算（单位 1%） */
    private BigDecimal finalAtkRatioAdd = BigDecimal.ZERO;
    /** 最终生命比例加算（单位 1%） */
    private BigDecimal finalHpRatioAdd = BigDecimal.ZERO;
    /** 最终防御比例加算（单位 1%） */
    private BigDecimal finalDefRatioAdd = BigDecimal.ZERO;

    /** 攻速增加加算（单位 1%，叠乘源） */
    private BigDecimal atkSpeedUpAdd = BigDecimal.ZERO;
    /** 攻速减少加算（单位 1%，叠乘源） */
    private BigDecimal atkSpeedDownAdd = BigDecimal.ZERO;

    public BigDecimal mergeFinalAtkRatio(BigDecimal roleRatio) {
        return mergeRatio(roleRatio, finalAtkRatioAdd);
    }

    public BigDecimal mergeFinalHpRatio(BigDecimal roleRatio) {
        return mergeRatio(roleRatio, finalHpRatioAdd);
    }

    public BigDecimal mergeFinalDefRatio(BigDecimal roleRatio) {
        return mergeRatio(roleRatio, finalDefRatioAdd);
    }

    private static BigDecimal mergeRatio(BigDecimal base, BigDecimal add) {
        BigDecimal b = base == null ? new BigDecimal("100") : base;
        BigDecimal a = add == null ? BigDecimal.ZERO : add;
        return b.add(a);
    }
}
