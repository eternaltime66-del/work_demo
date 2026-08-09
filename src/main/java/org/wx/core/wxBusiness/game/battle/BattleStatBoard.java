package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.battle.enums.BattleStatKey;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountDirection;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountScope;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 战斗演算用统计面板（读取侧）。
 */
public class BattleStatBoard {

    private int elapsedActionValue;

    private final Map<String, BattleUnitStats> unitStats = new LinkedHashMap<>();
    private final Map<BattleSkillCountKey, Integer> skillCounts = new LinkedHashMap<>();
    private final Map<String, long[]> dealDamage = new LinkedHashMap<>();
    private final Map<String, long[]> receiveDamage = new LinkedHashMap<>();
    private final Map<String, long[]> dealHeal = new LinkedHashMap<>();
    private final Map<String, long[]> receiveHeal = new LinkedHashMap<>();
    /** 主动技能伤害次数（被动产生不计）：deal / receive */
    private final Map<String, long[]> activeSkillDamageCount = new LinkedHashMap<>();

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
        return getSkillCount(roleId, direction, scope, skillId, skillType, null, null);
    }

    public int getSkillCount(String roleId, SkillCountDirection direction, SkillCountScope scope,
                             String skillId, ActiveSkillType skillType,
                             String skillSchool, DamageElement damageElement) {
        BattleSkillCountKey key = new BattleSkillCountKey();
        key.setRoleId(roleId);
        key.setDirection(direction);
        key.setScope(scope);
        key.setSkillId(skillId);
        key.setSkillType(skillType);
        if (scope == SkillCountScope.SKILL_SCHOOL) {
            key.setSkillSchool(SkillSchoolUnit.normalizeSchool(skillSchool));
        }
        if (scope == SkillCountScope.SKILL_ELEMENT) {
            key.setDamageElement(SkillSchoolUnit.normalizeElement(damageElement));
        }
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

    public void recordCast(String roleId, String skillId, ActiveSkillType skillType) {
        recordCast(roleId, skillId, skillType, null, null);
    }

    public void recordCast(String roleId, ActiveSkill skill) {
        if (skill == null) {
            return;
        }
        recordCast(roleId, skill.getId(), skill.getSkillType(),
                SkillSchoolUnit.schoolOf(skill), SkillSchoolUnit.elementOf(skill));
    }

    public void recordCast(String roleId, String skillId, ActiveSkillType skillType,
                           String skillSchool, DamageElement damageElement) {
        addSkillCount(BattleSkillCountKey.ofAny(roleId, SkillCountDirection.CAST), 1);
        if (skillType != null) {
            addSkillCount(BattleSkillCountKey.ofType(roleId, SkillCountDirection.CAST, skillType), 1);
        }
        addSkillCount(BattleSkillCountKey.ofSchool(roleId, SkillCountDirection.CAST, skillSchool), 1);
        addSkillCount(BattleSkillCountKey.ofElement(roleId, SkillCountDirection.CAST, damageElement), 1);
        if (skillId != null && !skillId.isBlank()) {
            addSkillCount(BattleSkillCountKey.ofSkill(roleId, SkillCountDirection.CAST, skillId), 1);
        }
    }

    public void recordReceive(String roleId, String skillId, ActiveSkillType skillType) {
        recordReceive(roleId, skillId, skillType, null, null);
    }

    public void recordReceive(String roleId, ActiveSkill skill) {
        if (skill == null) {
            return;
        }
        recordReceive(roleId, skill.getId(), skill.getSkillType(),
                SkillSchoolUnit.schoolOf(skill), SkillSchoolUnit.elementOf(skill));
    }

    public void recordReceive(String roleId, String skillId, ActiveSkillType skillType,
                              String skillSchool, DamageElement damageElement) {
        addSkillCount(BattleSkillCountKey.ofAny(roleId, SkillCountDirection.RECEIVE), 1);
        if (skillType != null) {
            addSkillCount(BattleSkillCountKey.ofType(roleId, SkillCountDirection.RECEIVE, skillType), 1);
        }
        addSkillCount(BattleSkillCountKey.ofSchool(roleId, SkillCountDirection.RECEIVE, skillSchool), 1);
        addSkillCount(BattleSkillCountKey.ofElement(roleId, SkillCountDirection.RECEIVE, damageElement), 1);
        if (skillId != null && !skillId.isBlank()) {
            addSkillCount(BattleSkillCountKey.ofSkill(roleId, SkillCountDirection.RECEIVE, skillId), 1);
        }
    }

    /** 记录伤害量与次数；fromActiveSkill=true 时额外累计主动技能伤害次数 */
    public void recordDamage(String dealerId, String receiverId, int amount, boolean fromActiveSkill) {
        if (amount <= 0) {
            return;
        }
        bumpDamage(dealDamage, dealerId, amount);
        bumpDamage(receiveDamage, receiverId, amount);
        if (fromActiveSkill) {
            bumpActiveCount(dealerId, true);
            bumpActiveCount(receiverId, false);
        }
    }

    public void recordDamage(String dealerId, String receiverId, int amount) {
        recordDamage(dealerId, receiverId, amount, true);
    }

    public long getDealDamageAmount(String roleId) {
        return damageAmount(dealDamage, roleId);
    }

    public long getDealDamageCount(String roleId) {
        return damageCount(dealDamage, roleId);
    }

    public long getReceiveDamageAmount(String roleId) {
        return damageAmount(receiveDamage, roleId);
    }

    public long getReceiveDamageCount(String roleId) {
        return damageCount(receiveDamage, roleId);
    }

    public long getActiveDealDamageCount(String roleId) {
        long[] arr = activeSkillDamageCount.get(roleId);
        return arr == null ? 0 : arr[0];
    }

    public long getActiveReceiveDamageCount(String roleId) {
        long[] arr = activeSkillDamageCount.get(roleId);
        return arr == null ? 0 : arr[1];
    }

    public void recordHeal(String healerId, String targetId, int amount) {
        if (amount <= 0) {
            return;
        }
        bumpDamage(dealHeal, healerId, amount);
        bumpDamage(receiveHeal, targetId, amount);
    }

    public long getDealHealAmount(String roleId) {
        return damageAmount(dealHeal, roleId);
    }

    public long getDealHealCount(String roleId) {
        return damageCount(dealHeal, roleId);
    }

    public long getReceiveHealAmount(String roleId) {
        return damageAmount(receiveHeal, roleId);
    }

    public long getReceiveHealCount(String roleId) {
        return damageCount(receiveHeal, roleId);
    }

    private static void bumpDamage(Map<String, long[]> map, String roleId, int amount) {
        if (roleId == null || roleId.isBlank()) {
            return;
        }
        long[] arr = map.computeIfAbsent(roleId, k -> new long[2]);
        arr[0] += amount;
        arr[1] += 1;
    }

    private void bumpActiveCount(String roleId, boolean deal) {
        if (roleId == null || roleId.isBlank()) {
            return;
        }
        long[] arr = activeSkillDamageCount.computeIfAbsent(roleId, k -> new long[2]);
        if (deal) {
            arr[0] += 1;
        } else {
            arr[1] += 1;
        }
    }

    private static long damageAmount(Map<String, long[]> map, String roleId) {
        long[] arr = map.get(roleId);
        return arr == null ? 0 : arr[0];
    }

    private static long damageCount(Map<String, long[]> map, String roleId) {
        long[] arr = map.get(roleId);
        return arr == null ? 0 : arr[1];
    }
}
