package org.wx.core.wxBusiness.game.battle;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private int maxHp;
    private int hp;
    private int atk;
    private int def;
    /** 行动阈值：进度攒满此值则行动 */
    private int action;

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

    /** 周期被动本场触发次数：passiveId -> count */
    private final Map<String, Integer> periodicTriggerCount = new HashMap<>();

    /**
     * 周期阶梯/边沿状态：passiveId -> candidateKey -> [lastStep, wasTrue(0/1)]
     */
    private final Map<String, Map<String, int[]>> periodicEdgeState = new HashMap<>();

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
}
