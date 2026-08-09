package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.annotation.RedisLock;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.battle.BattleEngine;
import org.wx.core.wxBusiness.game.battle.BattleRuntimeUnit;
import org.wx.core.wxBusiness.game.battle.BattleSide;
import org.wx.core.wxBusiness.game.battle.DamageRatioUnit;
import org.wx.core.wxBusiness.game.battle.FinalStatCalcUnit;
import org.wx.core.wxBusiness.game.battle.PassiveConditionEvalUnit;
import org.wx.core.wxBusiness.game.entity.vo.EquipBonusVo;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.PlayerLayout;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.entity.PlayerTowerRun;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.StageModeCode;
import org.wx.core.wxBusiness.game.entity.enums.TowerRunStatus;
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
    @Resource
    private SkillOutputService skillOutputService;
    @Resource
    private BuffDefService buffDefService;
    @Resource
    private StageService stageService;
    @Resource
    private StageProgressService stageProgressService;
    @Resource
    private TowerRunService towerRunService;
    @Resource
    private PlayerStaminaService playerStaminaService;

    /**
     * 主线 / 普通选关战斗（无尽塔请走 {@link #towerFight}）
     */
    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid", bindMethod = false, loading = true, leaseSeconds = 300)
    public BattleResultVo fight(String uid, String levelId) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        ErrorFactory.throwError(Wx.isEmpty(levelId), "关卡不能为空");
        Stage level = stageService.requireLevel(levelId);
        Stage type = stageProgressService.resolveTypeRoot(level);
        ErrorFactory.throwError(StageModeCode.TOWER.equals(type.getCode()), "无尽塔请从爬塔入口进入");

        PlayerStamina stamina = null;
        if (StageModeCode.MAIN.equals(type.getCode())) {
            stamina = stageProgressService.prepareMainlineFight(uid, level);
        }

        BattleResultVo result = runFight(uid, levelId, null);
        if (stamina != null) {
            PlayerStamina latest = playerStaminaService.getOrCreate(uid);
            result.setStaminaLeft(latest.getStamina());
            result.setStaminaMax(latest.getMaxStamina());
        }
        if (StageModeCode.MAIN.equals(type.getCode()) && "WIN".equals(result.getOutcome())) {
            StageProgressService.MainlineWinReward reward = stageProgressService.onMainlineWin(uid, level);
            result.setFirstClear(reward.isFirstClear());
            result.setLevelFirstRewards(reward.getLevelFirstRewards());
            result.setChapterFirstRewards(reward.getChapterFirstRewards());
            if (!reward.getLevelFirstRewards().isEmpty()) {
                result.getLogs().add("领取小关首通奖励 ×" + reward.getLevelFirstRewards().size());
            }
            if (!reward.getChapterFirstRewards().isEmpty()) {
                result.getLogs().add("领取大关首通奖励 ×" + reward.getChapterFirstRewards().size());
            }
        }
        return result;
    }

    /**
     * 无尽塔：打当前层；胜利保留残血进入下一层，失败结束本次爬塔。
     */
    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid", bindMethod = false, loading = true, leaseSeconds = 300)
    public BattleResultVo towerFight(String uid) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        PlayerTowerRun run = towerRunService.getByUid(uid);
        Stage level = towerRunService.requireCurrentLevel(run);
        towerRunService.assertIsTowerLevel(level);

        Map<String, Integer> allyHp = towerRunService.readAllyHp(run);
        BattleResultVo result = runFight(uid, level.getId(), allyHp);

        if ("WIN".equals(result.getOutcome())) {
            Map<String, Integer> remain = new HashMap<>();
            if (allyHp != null) {
                remain.putAll(allyHp);
            }
            if (result.getAllyRemainHp() != null) {
                remain.putAll(result.getAllyRemainHp());
            }
            towerRunService.onWin(run, remain);
            PlayerTowerRun after = towerRunService.getByUid(uid);
            result.setTowerStatus(after.getStatus() != null ? after.getStatus().name() : null);
            result.setTowerNextLevelId(after.getCurrentLevelId());
            if (!Wx.isEmpty(after.getCurrentLevelId())) {
                Stage next = stageService.getById(after.getCurrentLevelId());
                if (next != null) {
                    result.setTowerNextLevelCode(next.getCode());
                }
                result.getLogs().add("进入下一层: " + (result.getTowerNextLevelCode() != null
                        ? result.getTowerNextLevelCode() : after.getCurrentLevelId()));
            } else if (after.getStatus() == TowerRunStatus.CLEARED) {
                result.getLogs().add("无尽塔通关");
            }
        } else {
            towerRunService.onLose(run);
            result.setTowerStatus(TowerRunStatus.DEAD.name());
            result.getLogs().add("无尽塔挑战失败");
        }
        return result;
    }

    private BattleResultVo runFight(String uid, String levelId, Map<String, Integer> allyHpOverride) {
        ActiveSkill defaultNormal = activeSkillService.ensureDefaultNormalSkill();
        ActiveSkill weaponNormal = playerEquipService.resolveWeaponNormalSkill(uid);
        List<ActiveSkill> equipChargeSkills = playerEquipService.resolveEquippedDefaultSkills(uid);
        List<PassiveSkill> equippedBattleStart = playerEquipService.resolveEquippedBattleStartPassives(uid);
        List<PassiveSkill> equippedBattleJudge = playerEquipService.resolveEquippedBattleJudgePassives(uid);
        List<PassiveSkill> equippedBattlePulse = playerEquipService.resolveEquippedBattlePulsePassives(uid);
        List<PassiveSkill> equippedBattleCombat = playerEquipService.resolveEquippedBattleCombatPassives(uid);
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
        List<PassiveSkill> battleStarts = filterEquipPassives(
                equippedBattleStart, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
        List<PassiveSkill> battleJudges = filterEquipPassives(
                equippedBattleJudge, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
        List<PassiveSkill> battlePulses = filterEquipPassives(
                equippedBattlePulse, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
        List<PassiveSkill> battleCombats = filterEquipPassives(
                equippedBattleCombat, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes);
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
            unit.setCode(Boolean.TRUE.equals(role.getMainRole()) ? "hero" : role.getId());
            unit.setPosCol(nvl(layout.getPosCol()));
            unit.setPosRow(nvl(layout.getPosRow()));
            unit.setGridW(Math.max(1, nvl(role.getGridW(), 1)));
            unit.setGridH(Math.max(1, nvl(role.getGridH(), 1)));
            int sumHp = nvl(role.getBaseHp()) + nvl(role.getExtraHp()) + equipBonus.getHp();
            int sumAtk = nvl(role.getBaseAtk()) + nvl(role.getExtraAtk()) + equipBonus.getAtk();
            int sumDef = nvl(role.getBaseDef()) + nvl(role.getExtraDef()) + equipBonus.getDefense();
            unit.setMaxHp(FinalStatCalcUnit.applyHp(sumHp, equipBonus.mergeFinalHpRatio(role.getFinalHpRatio())));
            int startHp = unit.getMaxHp();
            if (allyHpOverride != null && allyHpOverride.containsKey(role.getId())) {
                Integer ov = allyHpOverride.get(role.getId());
                if (ov != null) {
                    startHp = Math.max(0, Math.min(unit.getMaxHp(), ov));
                }
            }
            unit.setHp(startHp);
            if (startHp <= 0) {
                allyIdx--;
                continue;
            }
            unit.setAtk(Math.max(0, FinalStatCalcUnit.apply(sumAtk, equipBonus.mergeFinalAtkRatio(role.getFinalAtkRatio()))));
            unit.setDef(Math.max(0, FinalStatCalcUnit.apply(sumDef, equipBonus.mergeFinalDefRatio(role.getFinalDefRatio()))));
            seedDamageRatios(unit, role, equipBonus);
            unit.setAction(atkSpeedService.calcRoleAction(uid, role, equipBonus));
            unit.setBaseAction(unit.getAction());
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
            unit.getBattleStartPassives().addAll(battleStarts);
            unit.getBattleJudgePassives().addAll(battleJudges);
            unit.getBattlePulsePassives().addAll(battlePulses);
            unit.getBattleCombatPassives().addAll(battleCombats);
            engine.addUnit(unit);
        }
        ErrorFactory.throwError(allyIdx == 0, "没有可用的上阵角色（无尽塔残血可能已全灭）");

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
            unit.setCode(monster.getId());
            if (monster.getRarity() != null) {
                unit.setRarity(monster.getRarity().name());
            }
            unit.setPosCol(nvl(sm.getPosCol()));
            unit.setPosRow(nvl(sm.getPosRow()));
            unit.setGridW(Math.max(1, nvl(monster.getGridW(), 1)));
            unit.setGridH(Math.max(1, nvl(monster.getGridH(), 1)));
            unit.setMaxHp(Math.max(1, nvl(monster.getBaseHp())));
            unit.setHp(unit.getMaxHp());
            unit.setAtk(Math.max(0, nvl(monster.getBaseAtk())));
            unit.setDef(Math.max(0, nvl(monster.getBaseDef())));
            unit.setAction(Math.max(1, nvl(monster.getBaseAction(), 10)));
            unit.setBaseAction(unit.getAction());
            bindMonsterSkills(unit, monster, defaultNormal, skillIds);
            engine.addUnit(unit);
        }

        for (String skillId : skillIds) {
            engine.putSkillMeta(skillId,
                    skillChargeService.listBySkillId(skillId),
                    skillEffectService.listBySkillId(skillId));
            List<SkillOutput> outs = skillOutputService.listBySkillId(skillId);
            if (outs != null && !outs.isEmpty()) {
                engine.putSkillOutputs(skillId, outs);
            }
        }

        // 开战挂载精简树：[角色] → 充能技能 / 被动技能（详情点名称查看）
        appendMountSummaryLogs(
                engine,
                resolveMainAllyName(engine),
                equipChargeSkills,
                mergeMountPassives(battleStarts, battleJudges, battlePulses, battleCombats)
        );
        seedV2Meta(engine, battleStarts, battleJudges, battlePulses, battleCombats);

        List<BattleUnitSnapVo> unitSnaps = engine.snapshotUnits();
        BattleResultVo result = engine.run();
        result.setUnits(unitSnaps);
        result.setAllyRemainHp(collectAllyRemainHp(engine));
        if ("WIN".equals(result.getOutcome())) {
            List<String> killed = engine.killedEnemySourceIds();
            List<MonsterDropResultVo> drops = monsterDropService.rollAndGrantToWarehouse(uid, killed);
            result.setDrops(drops);
            appendVictoryDropLogs(result.getLogs(), drops);
            attachEndDrops(result, drops);
        } else if ("LOSE".equals(result.getOutcome())) {
            result.getLogs().add("战斗失败");
        }
        return result;
    }

    private Map<String, Integer> collectAllyRemainHp(BattleEngine engine) {
        Map<String, Integer> map = new HashMap<>();
        if (engine == null || engine.units() == null) {
            return map;
        }
        for (BattleRuntimeUnit u : engine.units()) {
            if (u == null || u.getSide() != BattleSide.ALLY || Wx.isEmpty(u.getSourceId())) {
                continue;
            }
            map.put(u.getSourceId(), Math.max(0, u.getHp()));
        }
        return map;
    }

    private void attachEndDrops(BattleResultVo result, List<MonsterDropResultVo> drops) {
        if (result == null || result.getEvents() == null || result.getEvents().isEmpty()) {
            return;
        }
        for (int i = result.getEvents().size() - 1; i >= 0; i--) {
            var ev = result.getEvents().get(i);
            if (ev != null && "BATTLE_END".equals(ev.getType())) {
                ev.setDrops(drops != null ? drops : List.of());
                return;
            }
        }
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

    /** 怪物技能槽：普攻（空则通用默认）+ 可选小技能/大招 */
    private void bindMonsterSkills(BattleRuntimeUnit unit,
                                   Monster monster,
                                   ActiveSkill defaultNormal,
                                   Set<String> skillIds) {
        if (unit == null || monster == null) {
            return;
        }
        ActiveSkill normal = resolveTypedSkill(monster.getNormalSkillId(), ActiveSkillType.NORMAL);
        if (normal == null) {
            normal = defaultNormal;
        }
        appendUnitSkill(unit, skillIds, normal);
        appendUnitSkill(unit, skillIds, resolveTypedSkill(monster.getSmallSkillId(), ActiveSkillType.SMALL));
        appendUnitSkill(unit, skillIds, resolveTypedSkill(monster.getUltimateSkillId(), ActiveSkillType.ULTIMATE));
    }

    private ActiveSkill resolveTypedSkill(String skillId, ActiveSkillType expectType) {
        if (Wx.isEmpty(skillId)) {
            return null;
        }
        ActiveSkill skill = activeSkillService.getById(skillId);
        if (skill == null) {
            return null;
        }
        if (expectType != null && skill.getSkillType() != null && skill.getSkillType() != expectType) {
            return null;
        }
        return skill;
    }

    private void appendUnitSkill(BattleRuntimeUnit unit, Set<String> skillIds, ActiveSkill skill) {
        if (unit == null || skill == null || Wx.isEmpty(skill.getId())) {
            return;
        }
        boolean exists = unit.getSkills().stream().anyMatch(s -> s != null && skill.getId().equals(s.getId()));
        if (!exists) {
            unit.getSkills().add(skill);
        }
        if (skillIds != null) {
            skillIds.add(skill.getId());
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
     * 开战挂载精简树：
     * <pre>
     * [主角]
     *  └ 充能技能
     *  │  └ 1. {{a:id}}重击
     *  └ 被动技能
     *  │  └ 1. {{p:id}}热身完毕
     * </pre>
     * 前端将 {{a/p:id}} 渲染为可点击名称。
     */
    private void appendMountSummaryLogs(
            BattleEngine engine,
            String roleName,
            List<ActiveSkill> chargeSkills,
            List<PassiveSkill> passives
    ) {
        if (engine == null) {
            return;
        }
        boolean hasCharge = chargeSkills != null && !chargeSkills.isEmpty();
        boolean hasPassive = passives != null && !passives.isEmpty();
        if (!hasCharge && !hasPassive) {
            return;
        }
        String owner = Wx.isEmpty(roleName) ? "角色" : roleName;
        engine.addLog("[" + owner + "]");
        if (hasCharge) {
            engine.addLog(" └ 充能技能");
            int idx = 0;
            for (ActiveSkill s : chargeSkills) {
                if (s == null || Wx.isEmpty(s.getId())) {
                    continue;
                }
                idx++;
                String name = s.getName() != null ? s.getName() : s.getId();
                engine.addLog(" │  └ " + idx + ". {{a:" + s.getId() + "}}" + name);
            }
        }
        if (hasPassive) {
            engine.addLog(" └ 被动技能");
            int idx = 0;
            for (PassiveSkill p : passives) {
                if (p == null || Wx.isEmpty(p.getId())) {
                    continue;
                }
                idx++;
                String name = p.getName() != null ? p.getName() : p.getId();
                engine.addLog(" │  └ " + idx + ". {{p:" + p.getId() + "}}" + name);
            }
        }
    }

    @SafeVarargs
    private final List<PassiveSkill> mergeMountPassives(List<PassiveSkill>... lists) {
        List<PassiveSkill> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (lists == null) {
            return out;
        }
        for (List<PassiveSkill> list : lists) {
            if (list == null) {
                continue;
            }
            for (PassiveSkill p : list) {
                if (p == null || Wx.isEmpty(p.getId()) || !seen.add(p.getId())) {
                    continue;
                }
                out.add(p);
            }
        }
        return out;
    }

    /** 取己方主角名；无主角则取首位己方单位名 */
    private String resolveMainAllyName(BattleEngine engine) {
        if (engine == null || engine.units() == null) {
            return null;
        }
        String first = null;
        for (BattleRuntimeUnit u : engine.units()) {
            if (u == null || u.getSide() != BattleSide.ALLY) {
                continue;
            }
            if ("hero".equals(u.getCode())) {
                return u.getName();
            }
            if (first == null) {
                first = u.getName();
            }
        }
        return first;
    }

    /** 注入 V2 被动输出与 BUFF 定义缓存 */
    private void seedV2Meta(
            BattleEngine engine,
            List<PassiveSkill> starts,
            List<PassiveSkill> judges,
            List<PassiveSkill> pulses,
            List<PassiveSkill> combats
    ) {
        if (engine == null) {
            return;
        }
        Set<String> buffIds = new HashSet<>();
        collectPassiveOutputs(engine, starts, buffIds);
        collectPassiveOutputs(engine, judges, buffIds);
        collectPassiveOutputs(engine, pulses, buffIds);
        collectPassiveOutputs(engine, combats, buffIds);
        for (String skillId : engine.skillOutputSkillIds()) {
            List<SkillOutput> outs = engine.skillOutputsOf(skillId);
            if (outs == null) {
                continue;
            }
            for (SkillOutput o : outs) {
                if (o != null && !Wx.isEmpty(o.getBuffDefId())) {
                    buffIds.add(o.getBuffDefId());
                }
            }
        }
        for (String buffId : buffIds) {
            BuffDef def = buffDefService.getById(buffId);
            if (def != null) {
                engine.putBuffDef(def);
            }
        }
    }

    private void collectPassiveOutputs(BattleEngine engine, List<PassiveSkill> passives, Set<String> buffIds) {
        if (passives == null) {
            return;
        }
        for (PassiveSkill p : passives) {
            if (p == null || p.getId() == null) {
                continue;
            }
            List<SkillOutput> outs = p.getOutputs();
            if (outs == null || outs.isEmpty()) {
                outs = skillOutputService.listByPassiveSkillId(p.getId());
                p.setOutputs(outs);
            }
            if (outs == null) {
                continue;
            }
            for (SkillOutput o : outs) {
                if (o != null && !Wx.isEmpty(o.getBuffDefId())) {
                    buffIds.add(o.getBuffDefId());
                }
            }
        }
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

    /**
     * 角色底板比例 × 战斗外被动叠乘积 → 开战伤害乘数。
     */
    private void seedDamageRatios(BattleRuntimeUnit unit, PlayerRole role, EquipBonusVo bonus) {
        if (unit == null) {
            return;
        }
        double deal = DamageRatioUnit.ratioToMult(role == null ? null : role.getDealDmgRatio());
        double taken = DamageRatioUnit.ratioToMult(role == null ? null : role.getTakenDmgRatio());
        double dealEl = DamageRatioUnit.ratioToMult(role == null ? null : role.getDealElementDmgRatio());
        double takenEl = DamageRatioUnit.ratioToMult(role == null ? null : role.getTakenElementDmgRatio());
        double dealPhys = DamageRatioUnit.ratioToMult(role == null ? null : role.getDealPhysDmgRatio());
        double takenPhys = DamageRatioUnit.ratioToMult(role == null ? null : role.getTakenPhysDmgRatio());
        if (bonus != null) {
            deal *= Math.max(0.0001D, bonus.getDealDmgMult());
            taken *= Math.max(0.0001D, bonus.getTakenDmgMult());
            dealEl *= Math.max(0.0001D, bonus.getDealElementDmgMult());
            takenEl *= Math.max(0.0001D, bonus.getTakenElementDmgMult());
            dealPhys *= Math.max(0.0001D, bonus.getDealPhysDmgMult());
            takenPhys *= Math.max(0.0001D, bonus.getTakenPhysDmgMult());
        }
        unit.setDealDmgMult(deal);
        unit.setTakenDmgMult(taken);
        unit.setDealElementDmgMult(dealEl);
        unit.setTakenElementDmgMult(takenEl);
        unit.setDealPhysDmgMult(dealPhys);
        unit.setTakenPhysDmgMult(takenPhys);
        if (role != null && role.getLifeStealRatio() != null) {
            unit.setLifeStealAdd(role.getLifeStealRatio().intValue());
        }
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private int nvl(Integer v, int def) {
        return v == null ? def : v;
    }
}




