package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.battle.enums.BattleStatKey;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountDirection;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountScope;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 战斗演算用统计面板（读取侧）。
 * <p>
 * 维度说明：
 * <ul>
 *   <li>角色属性：每个角色 × {最大生命/当前生命/攻击/防御/行动值}</li>
 *   <li>已经过行动值：整场战斗一条累加值</li>
 *   <li>充能技能次数：每个角色 × {释放/受到} × {指定技能/指定类型/任意}</li>
 * </ul>
 */
public class BattleStatBoard {

    /** 本次战斗已经过行动值 */
    private int elapsedActionValue;

    private final Map<String, BattleUnitStats> unitStats = new LinkedHashMap<>();
    private final Map<BattleSkillCountKey, Integer> skillCounts = new LinkedHashMap<>();

    public int getElapsedActionValue() {
        return elapsedActionValue;
    }

    public void setElapsedActionValue(int elapsedActionValue) {
        this.elapsedActionValue = Math.max(0, elapsedActionValue);
    }

    public void addElapsedAction(int delta) {
        if (delta > 0) {
            this.elapsedActionValue += delta;
        }
    }

    public void putUnit(BattleUnitStats stats) {
        if (stats != null && stats.getRoleId() != null) {
            unitStats.put(stats.getRoleId(), stats);
        }
    }

    public BattleUnitStats getUnit(String roleId) {
        return unitStats.get(roleId);
    }

    public Collection<BattleUnitStats> allUnits() {
        return Collections.unmodifiableCollection(unitStats.values());
    }

    public int getStat(String roleId, BattleStatKey key) {
        BattleUnitStats u = unitStats.get(roleId);
        if (u == null || key == null) {
            return 0;
        }
        return switch (key) {
            case MAX_HP -> u.getMaxHp();
            case HP -> u.getHp();
            case ATK -> u.getAtk();
            case DEF -> u.getDef();
            case ACTION -> u.getAction();
        };
    }

    public int getSkillCount(BattleSkillCountKey key) {
        if (key == null) {
            return 0;
        }
        return skillCounts.getOrDefault(key, 0);
    }

    public int getSkillCount(String roleId, SkillCountDirection direction, SkillCountScope scope,
                             String skillId, ActiveSkillType skillType) {
        BattleSkillCountKey key = new BattleSkillCountKey();
        key.setRoleId(roleId);
        key.setDirection(direction);
        key.setScope(scope);
        key.setSkillId(skillId);
        key.setSkillType(skillType);
        return getSkillCount(key);
    }

    public void setSkillCount(BattleSkillCountKey key, int count) {
        if (key == null) {
            return;
        }
        skillCounts.put(key, Math.max(0, count));
    }

    public void addSkillCount(BattleSkillCountKey key, int delta) {
        if (key == null || delta == 0) {
            return;
        }
        setSkillCount(key, getSkillCount(key) + delta);
    }

    /**
     * 记录一次「释放」指定充能技能：同时累加 ANY / 类型 / 指定技能 三个维度
     */
    public void recordCast(String roleId, String skillId, ActiveSkillType skillType) {
        addSkillCount(BattleSkillCountKey.ofAny(roleId, SkillCountDirection.CAST), 1);
        if (skillType != null) {
            addSkillCount(BattleSkillCountKey.ofType(roleId, SkillCountDirection.CAST, skillType), 1);
        }
        if (skillId != null && !skillId.isBlank()) {
            addSkillCount(BattleSkillCountKey.ofSkill(roleId, SkillCountDirection.CAST, skillId), 1);
        }
    }

    /**
     * 记录一次「受到」指定充能技能
     */
    public void recordReceive(String roleId, String skillId, ActiveSkillType skillType) {
        addSkillCount(BattleSkillCountKey.ofAny(roleId, SkillCountDirection.RECEIVE), 1);
        if (skillType != null) {
            addSkillCount(BattleSkillCountKey.ofType(roleId, SkillCountDirection.RECEIVE, skillType), 1);
        }
        if (skillId != null && !skillId.isBlank()) {
            addSkillCount(BattleSkillCountKey.ofSkill(roleId, SkillCountDirection.RECEIVE, skillId), 1);
        }
    }
}
