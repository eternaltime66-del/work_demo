package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 战斗生命周期锚点事件（BATTLE_COMBAT）。
 */
public enum CombatEventType {
    AFTER_DEAL_ACTIVE_DMG("造成主动技能伤害后"),
    AFTER_TAKE_ACTIVE_DMG("受到主动技能伤害后"),
    AFTER_CAST_SKILL("释放技能后"),
    AFTER_RECEIVE_SKILL("受到技能后"),
    AFTER_TAKE_PULSE_BUFF_DMG("受到脉冲BUFF伤害后"),
    AFTER_KILL("击杀时"),
    AFTER_KILLED("被击杀时"),
    /** 持有闪避效果并成功闪避一次伤害后（触发方=闪避者） */
    AFTER_DODGE("触发闪避后");

    private final String label;

    CombatEventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
