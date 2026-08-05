package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.battle.BattleEngine;
import org.wx.core.wxBusiness.game.battle.BattleRuntimeUnit;
import org.wx.core.wxBusiness.game.battle.BattleSide;
import org.wx.core.wxBusiness.game.battle.FinalStatCalcUnit;
import org.wx.core.wxBusiness.game.battle.PassiveConditionEvalUnit;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.PlayerLayout;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleUnitSnapVo;
import org.wx.core.wxBusiness.game.entity.vo.MonsterDropResultVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 战斗入口：组装布阵/关卡怪物 → 行动值演算 → 胜利掉落入仓库
 */
@Service
public class BattleService {

    @Resource
    private PlayerLayoutService playerLayoutService;
    @Resource
    private PlayerRoleService playerRoleService;
    @Resource
    private PlayerRoleSkillService playerRoleSkillService;
    @Resource
    private ActiveSkillService activeSkillService;
    @Resource
    private SkillChargeService skillChargeService;
    @Resource
    private SkillEffectService skillEffectService;
    @Resource
    private StageLevelMonsterService stageLevelMonsterService;
    @Resource
    private MonsterService monsterService;
    @Resource
    private MonsterDropService monsterDropService;
    @Resource
    private AtkSpeedService atkSpeedService;
    @Resource
    private PlayerEquipService playerEquipService;
    @Resource
    private EquipBonusService equipBonusService;

    @Transactional(rollbackFor = Exception.class)
    public BattleResultVo fight(String uid, String levelId) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        ErrorFactory.throwError(Wx.isEmpty(levelId), "关卡不能为空");

        playerRoleService.ensureAllRolesNormalSkill(uid);
        ActiveSkill defaultNormal = activeSkillService.ensureDefaultNormalSkill();
        ActiveSkill weaponNormal = playerEquipService.resolveWeaponNormalSkill(uid);
        List<ActiveSkill> equipChargeSkills = playerEquipService.resolveEquippedDefaultSkills(uid);
        List<PassiveSkill> equippedAnchors = playerEquipService.resolveEquippedAnchorPassives(uid);
        List<PassiveSkill> equippedPeriodics = playerEquipService.resolveEquippedPeriodicPassives(uid);
        Set<String> equippedItemIds = playerEquipService.resolveEquippedItemIds(uid);
        Set<ItemType> equippedItemTypes = playerEquipService.resolveEquippedItemTypes(uid);
        Set<String> equippedSkillIds = new HashSet<>();
        Set<ActiveSkillType> equippedSkillTypes = new HashSet<>();
        if (weaponNormal != null && weaponNormal.getId() != null) {
            equippedSkillIds.add(weaponNormal.getId());
            if (weaponNormal.getSkillType() != null) {
                equippedSkillTypes.add(weaponNormal.getSkillType());
            }
        }
        if (equipChargeSkills != null) {
            for (ActiveSkill s : equipChargeSkills) {
                if (s == null || s.getId() == null) {
                    continue;
                }
                equippedSkillIds.add(s.getId());
                if (s.getSkillType() != null) {
                    equippedSkillTypes.add(s.getSkillType());
                }
            }
        }
        List<PassiveSkill> battleAnchors = filterEquipPassives(
                equippedAnchors, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
        List<PassiveSkill> battlePeriodics = filterEquipPassives(
                equippedPeriodics, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
        var equipBonus = equipBonusService.sumBonus(uid);

        List<PlayerLayout> layouts = playerLayoutService.listByUid(uid);
        ErrorFactory.throwError(layouts == null || layouts.isEmpty(), "请先在布局中上阵角色");

        List<StageLevelMonster> stageMonsters = stageLevelMonsterService.listByLevelId(levelId);
        ErrorFactory.throwError(stageMonsters == null || stageMonsters.isEmpty(), "该关卡暂无怪物");

        BattleEngine engine = new BattleEngine();
        Set<String> skillIds = new HashSet<>();

        int allyIdx = 0;
        for (PlayerLayout layout : layouts) {
            PlayerRole role = playerRoleService.getById(layout.getRoleId());
            if (role == null) {
                continue;
            }
            playerRoleService.prepareRatios(role);
            BattleRuntimeUnit unit = new BattleRuntimeUnit();
            unit.setUnitId("A" + (++allyIdx));
            unit.setSourceId(role.getId());
            unit.setName(role.getName());
            unit.setSide(BattleSide.ALLY);
            unit.setPosCol(nvl(layout.getPosCol()));
            unit.setPosRow(nvl(layout.getPosRow()));
            unit.setGridW(Math.max(1, nvl(role.getGridW(), 1)));
            unit.setGridH(Math.max(1, nvl(role.getGridH(), 1)));
            int sumHp = nvl(role.getBaseHp()) + nvl(role.getExtraHp()) + equipBonus.getHp();
            int sumAtk = nvl(role.getBaseAtk()) + nvl(role.getExtraAtk()) + equipBonus.getAtk();
            int sumDef = nvl(role.getBaseDef()) + nvl(role.getExtraDef()) + equipBonus.getDefense();
            unit.setMaxHp(FinalStatCalcUnit.applyHp(sumHp, equipBonus.mergeFinalHpRatio(role.getFinalHpRatio())));
            unit.setHp(unit.getMaxHp());
            unit.setAtk(Math.max(0, FinalStatCalcUnit.apply(sumAtk, equipBonus.mergeFinalAtkRatio(role.getFinalAtkRatio()))));
            unit.setDef(Math.max(0, FinalStatCalcUnit.apply(sumDef, equipBonus.mergeFinalDefRatio(role.getFinalDefRatio()))));
            unit.setAction(atkSpeedService.calcRoleAction(uid, role, equipBonus));
            List<ActiveSkill> skills = applyWeaponNormalOverride(
                    playerRoleSkillService.listSkillsByRoleId(role.getId()),
                    weaponNormal,
                    defaultNormal
            );
            mergeEquipChargeSkills(skills, equipChargeSkills);
            unit.getSkills().addAll(skills);
            skills.forEach(s -> {
                if (s != null && s.getId() != null) {
                    skillIds.add(s.getId());
                }
            });
            unit.getAnchorPassives().addAll(battleAnchors);
            unit.getPeriodicPassives().addAll(battlePeriodics);
            engine.addUnit(unit);
        }
        ErrorFactory.throwError(allyIdx == 0, "没有可用的上阵角色");

        List<Monster> enemyMonsters = new ArrayList<>();
        List<StageLevelMonster> enemySlots = new ArrayList<>();
        for (StageLevelMonster sm : stageMonsters) {
            Monster monster = monsterService.getById(sm.getMonsterId());
            if (monster == null) {
                continue;
            }
            enemyMonsters.add(monster);
            enemySlots.add(sm);
        }
        ErrorFactory.throwError(enemyMonsters.isEmpty(), "关卡怪物无效");
        Map<String, Integer> nameTotal = new HashMap<>();
        for (Monster monster : enemyMonsters) {
            String base = Wx.isEmpty(monster.getName()) ? "怪物" : monster.getName();
            nameTotal.merge(base, 1, Integer::sum);
        }
        Map<String, Integer> nameSeq = new HashMap<>();
        for (int i = 0; i < enemyMonsters.size(); i++) {
            Monster monster = enemyMonsters.get(i);
            StageLevelMonster sm = enemySlots.get(i);
            String base = Wx.isEmpty(monster.getName()) ? "怪物" : monster.getName();
            String displayName = base;
            if (nameTotal.getOrDefault(base, 1) > 1) {
                int seq = nameSeq.merge(base, 1, Integer::sum);
                displayName = base + seq;
            }
            BattleRuntimeUnit unit = new BattleRuntimeUnit();
            unit.setUnitId("E" + (i + 1));
            unit.setSourceId(monster.getId());
            unit.setName(displayName);
            unit.setSide(BattleSide.ENEMY);
            unit.setPosCol(nvl(sm.getPosCol()));
            unit.setPosRow(nvl(sm.getPosRow()));
            unit.setGridW(Math.max(1, nvl(monster.getGridW(), 1)));
            unit.setGridH(Math.max(1, nvl(monster.getGridH(), 1)));
            unit.setMaxHp(Math.max(1, nvl(monster.getBaseHp())));
            unit.setHp(unit.getMaxHp());
            unit.setAtk(Math.max(0, nvl(monster.getBaseAtk())));
            unit.setDef(Math.max(0, nvl(monster.getBaseDef())));
            unit.setAction(Math.max(1, nvl(monster.getBaseAction(), 10)));
            if (defaultNormal != null) {
                unit.getSkills().add(defaultNormal);
                skillIds.add(defaultNormal.getId());
            }
            engine.addUnit(unit);
        }

        for (String skillId : skillIds) {
            engine.putSkillMeta(skillId,
                    skillChargeService.listBySkillId(skillId),
                    skillEffectService.listBySkillId(skillId));
        }

        if (equipChargeSkills != null && !equipChargeSkills.isEmpty()) {
            StringBuilder sb = new StringBuilder("装备充能技能：");
            for (int i = 0; i < equipChargeSkills.size(); i++) {
                ActiveSkill s = equipChargeSkills.get(i);
                if (s == null) {
                    continue;
                }
                if (i > 0) {
                    sb.append("；");
                }
                sb.append(formatEquipChargeSkillBrief(s));
            }
            engine.addLog(sb.toString());
        }
        if (!battleAnchors.isEmpty()) {
            StringBuilder sb = new StringBuilder("装备锚点被动：");
            for (int i = 0; i < battleAnchors.size(); i++) {
                PassiveSkill p = battleAnchors.get(i);
                if (p == null) {
                    continue;
                }
                if (i > 0) {
                    sb.append("；");
                }
                sb.append(p.getName() != null ? p.getName() : p.getId());
                if (p.getAnchorType() != null) {
                    sb.append("（").append(p.getAnchorType().getLabel()).append("）");
                }
            }
            engine.addLog(sb.toString());
        }
        if (!battlePeriodics.isEmpty()) {
            StringBuilder sb = new StringBuilder("装备周期被动：");
            for (int i = 0; i < battlePeriodics.size(); i++) {
                PassiveSkill p = battlePeriodics.get(i);
                if (p == null) {
                    continue;
                }
                if (i > 0) {
                    sb.append("；");
                }
                sb.append(p.getName() != null ? p.getName() : p.getId());
                if (p.getPeriodicTriggerMode() != null) {
                    sb.append("（").append(p.getPeriodicTriggerMode().getLabel()).append("）");
                }
            }
            engine.addLog(sb.toString());
        }

        List<BattleUnitSnapVo> unitSnaps = engine.snapshotUnits();
        BattleResultVo result = engine.run();
        result.setUnits(unitSnaps);
        if ("WIN".equals(result.getOutcome())) {
            List<String> killed = engine.killedEnemySourceIds();
            List<MonsterDropResultVo> drops = monsterDropService.rollAndGrantToWarehouse(uid, killed);
            result.setDrops(drops);
            appendVictoryDropLogs(result.getLogs(), drops);
        } else if ("LOSE".equals(result.getOutcome())) {
            result.getLogs().add("战斗失败");
        }
        return result;
    }

    /** 胜利日志：总计 + 逐条物品 */
    private void appendVictoryDropLogs(List<String> logs, List<MonsterDropResultVo> drops) {
        LinkedHashMap<String, Integer> nameQty = new LinkedHashMap<>();
        int totalQty = 0;
        if (drops != null) {
            for (MonsterDropResultVo d : drops) {
                if (d == null) {
                    continue;
                }
                int qty = d.getQuantity() == null ? 0 : Math.max(0, d.getQuantity());
                totalQty += qty;
                String name = !Wx.isEmpty(d.getItemName()) ? d.getItemName() : "未知物品";
                nameQty.merge(name, qty, Integer::sum);
            }
        }
        logs.add("战斗胜利 总计掉落物品 * " + totalQty);
        if (nameQty.isEmpty()) {
            logs.add("  └ 无掉落");
            return;
        }
        for (Map.Entry<String, Integer> e : nameQty.entrySet()) {
            logs.add("  └ " + e.getKey() + " * " + e.getValue());
        }
    }

    /**
     * 武器普攻槽有配置时，替换角色全部普攻为武器普攻；
     * 未配置（清空）时使用角色已有普攻，若无则补通用默认普攻。
     */
    private List<ActiveSkill> applyWeaponNormalOverride(List<ActiveSkill> roleSkills,
                                                        ActiveSkill weaponNormal,
                                                        ActiveSkill defaultNormal) {
        List<ActiveSkill> skills = new ArrayList<>();
        if (roleSkills != null) {
            skills.addAll(roleSkills);
        }
        if (weaponNormal != null) {
            skills.removeIf(s -> s != null && s.getSkillType() == ActiveSkillType.NORMAL);
            skills.add(0, weaponNormal);
            return skills;
        }
        boolean hasNormal = skills.stream().anyMatch(s -> s != null && s.getSkillType() == ActiveSkillType.NORMAL);
        if (!hasNormal && defaultNormal != null) {
            skills.add(0, defaultNormal);
        }
        return skills;
    }

    /** 合并装备默认充能技能（大招/小技能），按 id 去重 */
    private void mergeEquipChargeSkills(List<ActiveSkill> skills, List<ActiveSkill> equipSkills) {
        if (skills == null || equipSkills == null || equipSkills.isEmpty()) {
            return;
        }
        for (ActiveSkill s : equipSkills) {
            if (s == null || Wx.isEmpty(s.getId())) {
                continue;
            }
            boolean exists = skills.stream().anyMatch(x -> x != null && s.getId().equals(x.getId()));
            if (!exists) {
                skills.add(s);
            }
        }
    }

    /**
     * 例：重击（需3：释放·任意技能 ×3次）
     */
    private String formatEquipChargeSkillBrief(ActiveSkill skill) {
        String name = skill.getName() != null ? skill.getName() : skill.getId();
        int need = resolveDisplayedNeedCharge(skill);
        List<SkillCharge> charges = skillChargeService.listBySkillId(skill.getId());
        String cond = formatChargeConditionBrief(charges, need);
        if (need < 0) {
            // SELF_BASE_ACTION
            if (Wx.isEmpty(cond)) {
                return name + "（需自身行动值）";
            }
            return name + "（需自身行动值：" + cond + "）";
        }
        if (need <= 0 && Wx.isEmpty(cond)) {
            return name;
        }
        if (Wx.isEmpty(cond)) {
            return name + "（需" + need + "）";
        }
        if (need <= 0) {
            return name + "（" + cond + "）";
        }
        return name + "（需" + need + "：" + cond + "）";
    }

    private int resolveDisplayedNeedCharge(ActiveSkill skill) {
        NeedChargeMode mode = skill.getNeedChargeMode() == null ? NeedChargeMode.MANUAL : skill.getNeedChargeMode();
        if (mode == NeedChargeMode.SELF_BASE_ACTION) {
            // 开战摘要无法取具体角色行动值，标为「自身行动值」
            return -1;
        }
        return skill.getNeedCharge() == null ? 0 : Math.max(0, skill.getNeedCharge());
    }

    private String formatChargeConditionBrief(List<SkillCharge> charges, int need) {
        if (charges == null || charges.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (SkillCharge c : charges) {
            if (c == null || c.getConditionType() == null) {
                continue;
            }
            if (c.getConditionType() == ChargeConditionType.SKILL_CHARGE) {
                String event = c.getSkillChargeEvent() == SkillChargeEvent.RECEIVE ? "受到" : "释放";
                String match = formatSkillChargeMatchLabel(c);
                int gain = c.getChargeGain() == null ? 0 : Math.max(0, c.getChargeGain());
                if (gain <= 0) {
                    parts.add(event + "·" + match);
                    continue;
                }
                if (need > 0) {
                    int times = (need + gain - 1) / gain;
                    parts.add(event + "·" + match + " ×" + times + "次");
                } else {
                    parts.add(event + "·" + match + " 每次+" + gain);
                }
            } else if (c.getConditionType() == ChargeConditionType.ACTION_VALUE) {
                int every = c.getEveryActionValue() == null ? 0 : c.getEveryActionValue();
                int gain = c.getChargeGain() == null ? 0 : c.getChargeGain();
                if (every > 0 && gain > 0) {
                    parts.add("每" + every + "行动值+" + gain);
                }
            }
        }
        return parts.stream().filter(s -> !Wx.isEmpty(s)).collect(Collectors.joining(" / "));
    }

    private String formatSkillChargeMatchLabel(SkillCharge c) {
        SkillChargeMatchMode mode = c.getSkillChargeMatch() == null ? SkillChargeMatchMode.ANY : c.getSkillChargeMatch();
        return switch (mode) {
            case ANY -> "任意技能";
            case ANY_TYPE -> "任意" + skillTypeLabel(c.getMatchSkillType()) + "技能";
            case SPECIFIC -> {
                if (Wx.isEmpty(c.getMatchSkillId())) {
                    yield "指定技能";
                }
                ActiveSkill target = activeSkillService.getById(c.getMatchSkillId());
                yield target != null && !Wx.isEmpty(target.getName()) ? target.getName() : "指定技能";
            }
        };
    }

    private String skillTypeLabel(ActiveSkillType type) {
        if (type == null) {
            return "类型";
        }
        return switch (type) {
            case NORMAL -> "普攻";
            case ULTIMATE -> "大招";
            case SMALL -> "小技能";
        };
    }

    private List<PassiveSkill> filterEquipPassives(
            List<PassiveSkill> source,
            Set<String> equippedItemIds,
            Set<String> equippedSkillIds,
            Set<ItemType> equippedItemTypes,
            Set<ActiveSkillType> equippedSkillTypes
    ) {
        List<PassiveSkill> out = new ArrayList<>();
        if (source == null) {
            return out;
        }
        for (PassiveSkill p : source) {
            if (PassiveConditionEvalUnit.matchEquipConditions(
                    p, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes)) {
                out.add(p);
            }
        }
        return out;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private int nvl(Integer v, int def) {
        return v == null ? def : v;
    }
}
