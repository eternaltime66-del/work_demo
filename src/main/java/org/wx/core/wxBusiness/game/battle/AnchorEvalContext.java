package org.wx.core.wxBusiness.game.battle;

import lombok.Data;

import java.util.List;

/**
 * 公式求值上下文（锚点 / 周期 / 通用聚合）。
 */
@Data
public class AnchorEvalContext {

    /** 本次/本段伤害值 */
    private int hitDamage;

    /** 本次施法累计技能伤害 */
    private int skillDamage;

    /** 施法者（受到技能时可读） */
    private BattleRuntimeUnit caster;

    /** 伤害来源 */
    private BattleRuntimeUnit damageSource;

    /** 当前受击/被伤害目标（公式目标项） */
    private BattleRuntimeUnit hitTarget;

    /** 周期：特定目标 */
    private BattleRuntimeUnit specificTarget;

    /** 全场单位（全部敌方/己方聚合） */
    private List<BattleRuntimeUnit> allUnits;
}
