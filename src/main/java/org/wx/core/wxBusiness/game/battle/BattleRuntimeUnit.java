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
    /** 行动阈值：进度攒满此值则行动 */
    private int action;

    /** 开战时行动阈值（攻速 buff 重算基准） */
    private int baseAction;

    /** 当前行动进度 */
    private int actionProgress;

    /** skillId -> 当前充能 */
    private final Map<String, Integer> chargeMap = new HashMap<>();
    /** skillId -> 已释放次数 */
    private final Map<String, Integer> castCountMap = new HashMap<>();

    private final List<ActiveSkill> skills = new ArrayList<>();

    /** 开战已挂载的锚点被动（含条件与 combatEffects） */
    private final List<PassiveSkill> anchorPassives = new ArrayList<>();

    /** 开战已挂载的周期被动 */
    private final List<PassiveSkill> periodicPassives = new ArrayList<>();

    /** 开战已挂载的持续效果被动 */
    private final List<PassiveSkill> sustainedPassives = new ArrayList<>();

    /** 周期被动本场触发次数：passiveId -> count */
    private final Map<String, Integer> periodicTriggerCount = new HashMap<>();

    /**
     * 周期阶梯/边沿状态：passiveId -> candidateKey -> [lastStep, wasTrue(0/1)]
     */
    private final Map<String, Map<String, int[]>> periodicEdgeState = new HashMap<>();

    /** 已激活的持续效果被动 id */
    private final Set<String> sustainedActiveIds = new HashSet<>();

    /** 可撤销属性 buff */
    private final List<TimedAttrBuff> timedBuffs = new ArrayList<>();

    /** 吸血加算（百分点，如 10 = 10%） */
    private int lifeStealAdd;

    /** 攻速加算（百分点，正=加快） */
    private int atkSpeedAdd;

    /** 造成伤害叠乘（默认 1） */
    private double dealDmgMult = 1D;

    /** 受到伤害叠乘（默认 1） */
    private double takenDmgMult = 1D;

    /** 最终攻击/生命/防御比例加算（百分点）——开战已折入面板，战斗中 buff 用 appliedFlat */
    private int finalAtkAdd;
    private int finalHpAdd;
    private int finalDefAdd;

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
