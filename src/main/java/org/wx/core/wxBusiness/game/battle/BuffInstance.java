package org.wx.core.wxBusiness.game.battle;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

/**
 * 战斗中挂载的 BUFF 实例。
 */
@Data
public class BuffInstance {

    private String instanceId;

    private String buffDefId;

    private String buffCode;

    private String name;

    private BuffKind buffKind;

    private int stacks = 1;

    /** 到期已经过行动值；null=永久 */
    private Integer expireAtElapsed;

    /** 下一脉冲结算的已经过行动值 */
    private Integer nextPulseAt;

    private int pulseEveryAv;

    /** 施加时锁定的元素加成系数（如 0.2=20%） */
    private double lockedElementBonus;

    private DamageElement damageElement;

    private String casterUnitId;

    private AttrModifyKey attrKey;

    /** 当前层已应用到面板的平坦量（可撤回） */
    private int appliedFlatPerStack;

    /**
     * FINAL_ATK/HP/DEF 等百分比折算后累计到面板的绝对值（可正负）。
     * 到期/移除时扣回该值，禁止再按当前面板反算百分比。
     */
    private int appliedFlatTotal;

    private int ratioAddPerStack;

    private double ratioFactorPerStack = 1D;

    /** JUDGE_ATTR：效果当前是否生效 */
    private boolean judgeEffectActive;

    private boolean beneficial = true;

    private boolean dispelable = true;
}
