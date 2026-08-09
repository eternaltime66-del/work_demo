package org.wx.core.wxBusiness.game.battle;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 战斗中的单位运行时状态
 */
@Data
public class BattleRuntimeUnit {

    private String unitId;
    private String sourceId;
    private String name;
    private BattleSide side;
    private int posCol;
    private int posRow;
    private int gridW = 1;
    private int gridH = 1;
    /** 素材 / 头像用编码（可空） */
    private String code;
    /** 怪物稀有度名（可空） */
    private String rarity;

    private int maxHp;
    private int hp;
    private int atk;
    private int def;
    /** 普攻最大充能值；数值越低，普攻充满越快。 */
    private int action;

    /** 开战时行动阈值（攻速 buff 重算基准） */
    private int baseAction;

    /** skillId -> 当前充能 */
    private final Map<String, Integer> chargeMap = new HashMap<>();
    /** skillId -> 已释放次数 */
    private final Map<String, Integer> castCountMap = new HashMap<>();

    private final List<ActiveSkill> skills = new ArrayList<>();

    /** @deprecated 旧锚点列表 */
    @Deprecated
    private final List<PassiveSkill> anchorPassives = new ArrayList<>();
    /** @deprecated */
    @Deprecated
    private final List<PassiveSkill> periodicPassives = new ArrayList<>();
    /** @deprecated */
    @Deprecated
    private final List<PassiveSkill> sustainedPassives = new ArrayList<>();

    /** V2 战斗型被动 */
    private final List<PassiveSkill> battleStartPassives = new ArrayList<>();
    private final List<PassiveSkill> battleJudgePassives = new ArrayList<>();
    private final List<PassiveSkill> battlePulsePassives = new ArrayList<>();
    private final List<PassiveSkill> battleCombatPassives = new ArrayList<>();

    private final Map<String, Integer> periodicTriggerCount = new HashMap<>();
    private final Map<String, Map<String, int[]>> periodicEdgeState = new HashMap<>();
    private final Set<String> sustainedActiveIds = new HashSet<>();
    /** 判定锚点已激活 */
    private final Set<String> judgeActiveIds = new HashSet<>();
    /** 开战时间规则已触发：passiveId -> lastStep */
    private final Map<String, Integer> startRuleState = new HashMap<>();

    private final List<TimedAttrBuff> timedBuffs = new ArrayList<>();
    private final List<BuffInstance> buffs = new ArrayList<>();

    private int lifeStealAdd;
    private int atkSpeedAdd;
    /** 造成/受到伤害比例乘数，默认 1（100%） */
    private double dealDmgMult = 1D;
    private double takenDmgMult = 1D;
    /** 造成/受到元素伤害比例乘数，默认 1 */
    private double dealElementDmgMult = 1D;
    private double takenElementDmgMult = 1D;
    /** 造成/受到物理伤害比例乘数，默认 1 */
    private double dealPhysDmgMult = 1D;
    private double takenPhysDmgMult = 1D;
    private int finalAtkAdd;
    private int finalHpAdd;
    private int finalDefAdd;

    /** 元素伤害加成（百分点） */
    private int elementDmgBonus;
    private int elementPhysBonus;
    private int elementPoisonBonus;
    private int elementIgniteBonus;
    private int elementFreezeBonus;
    private int elementShockBonus;
    private int elementBurnBonus;
    /** 元素抗性（百分点） */
    private int resistPhys;
    private int resistPoison;
    private int resistIgnite;
    private int resistFreeze;
    private int resistShock;
    private int resistBurn;

    public boolean alive() {
        return hp > 0;
    }

    public int getCharge(String skillId) {
        return chargeMap.getOrDefault(skillId, 0);
    }

    public void addCharge(String skillId, int delta) {
        if (skillId == null || delta == 0) {
            return;
        }
        chargeMap.put(skillId, Math.max(0, getCharge(skillId) + delta));
    }

    public void spendCharge(String skillId, int cost) {
        if (cost <= 0) {
            return;
        }
        chargeMap.put(skillId, Math.max(0, getCharge(skillId) - cost));
    }

    public int getCastCount(String skillId) {
        return castCountMap.getOrDefault(skillId, 0);
    }

    public void incCast(String skillId) {
        castCountMap.put(skillId, getCastCount(skillId) + 1);
    }

    public TimedAttrBuff findBuff(String sourceKey) {
        if (sourceKey == null) {
            return null;
        }
        for (TimedAttrBuff b : timedBuffs) {
            if (b != null && sourceKey.equals(b.getSourceKey())) {
                return b;
            }
        }
        return null;
    }

}
