package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.PassiveCombatEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.BattleStartApplyRule;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeScope;
import org.wx.core.wxBusiness.game.entity.enums.CombatEventType;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.DamageSourceKind;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PeriodicTriggerMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.vo.BattleEventVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleSkillSlotVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleUnitSnapVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 行动值战斗演算：
 * <ul>
 *   <li>每 tick 全局已经过行动值 +1，各存活单位行动进度 +1</li>
 *   <li>进度达到自身行动阈值则行动：释放可用技能，结算效果</li>
 *   <li>敌方全灭胜利 / 我方全灭失败</li>
 *   <li>充能技能路径触发锚点被动；状态变更总线触发周期被动与持续效果</li>
 *   <li>ATTR_MODIFY 可挂时长 buff（按已经过行动值到期）；持续效果条件成立套用、不成立撤销</li>
 * </ul>
 */
public class BattleEngine implements SkillV2OutputUnit.Host {

    public static final int MAX_TICKS = 5000;
    private static final double STEP_EPS = 1e-9;

    private final List<BattleRuntimeUnit> units = new ArrayList<>();
    private final Map<String, List<SkillCharge>> chargesBySkill = new HashMap<>();
    private final Map<String, List<SkillEffect>> effectsBySkill = new HashMap<>();
    private final Map<String, List<SkillOutput>> outputsBySkill = new HashMap<>();
    private final Map<String, BuffDef> buffDefs = new HashMap<>();
    private final BattleStatBoard board = new BattleStatBoard();
    private final List<String> logs = new ArrayList<>();
    private final List<BattleEventVo> events = new ArrayList<>();

    /** 当前施法累计伤害统计（castSkill 期间） */
    private CastDamageBag castBag;
    /** 当前施法技能（供 V2 伤害标签） */
    private ActiveSkill castingSkill;
    /** 当前施法真正执行过的目标，避免随机目标在展示和结算阶段各掷一次。 */
    private LinkedHashSet<BattleRuntimeUnit> currentCastAffectedUnits;

    /** 当前同步触发链中正在执行的被动；阻止 A→A 或 A→B→A 的无限递归。 */
    private final Set<String> activeCombatTriggerKeys = new HashSet<>();
    /** 单条战斗事件链的硬上限，避免错误配置制造无限级联。 */
    private static final int MAX_COMBAT_TRIGGER_DEPTH = 64;
    private int combatTriggerDepth;

    /** 周期/持续扫描防重入 */
    private boolean scanningPeriodic;
    private boolean scanningSustained;
    private boolean scanningV2;
    private int notifyDepth;

    public void addUnit(BattleRuntimeUnit unit) {
        if (unit != null) {
            if (unit.getBaseAction() <= 0) {
                unit.setBaseAction(Math.max(1, unit.getAction()));
            }
            units.add(unit);
            BattleUnitStats snap = new BattleUnitStats();
            snap.setRoleId(unit.getUnitId());
            snap.setRoleName(unit.getName());
            snap.setMaxHp(unit.getMaxHp());
            snap.setHp(unit.getHp());
            snap.setAtk(unit.getAtk());
            snap.setDef(unit.getDef());
            snap.setAction(unit.getAction());
            board.putUnit(snap);
        }
    }

    public void putSkillMeta(String skillId, List<SkillCharge> charges, List<SkillEffect> effects) {
        if (skillId == null) {
            return;
        }
        chargesBySkill.put(skillId, charges == null ? List.of() : charges);
        effectsBySkill.put(skillId, effects == null ? List.of() : effects);
    }

    public void putSkillOutputs(String skillId, List<SkillOutput> outputs) {
        if (skillId == null) {
            return;
        }
        outputsBySkill.put(skillId, outputs == null ? List.of() : outputs);
    }

    public void putBuffDef(BuffDef def) {
        if (def != null && def.getId() != null) {
            buffDefs.put(def.getId(), def);
        }
    }

    public Set<String> skillOutputSkillIds() {
        return outputsBySkill.keySet();
    }

    public List<SkillOutput> skillOutputsOf(String skillId) {
        return outputsBySkill.get(skillId);
    }

    public void addLog(String line) {
        if (line != null && !line.isBlank()) {
            logs.add(line);
        }
    }

    @Override
    public List<BattleRuntimeUnit> units() {
        return units;
    }

    @Override
    public BattleStatBoard board() {
        return board;
    }

    @Override
    public List<String> logs() {
        return logs;
    }

    @Override
    public BattleEventVo emit(String type) {
        return emitInternal(type);
    }

    @Override
    public void notifyStatChanged() {
        notifyStatChangedInternal();
    }

    @Override
    public void onAttackSpeedChanged(BattleRuntimeUnit unit) {
        recalcActionFromAtkSpeed(unit);
    }

    @Override
    public void recordAffectedTarget(BattleRuntimeUnit unit) {
        if (unit != null && currentCastAffectedUnits != null) {
            currentCastAffectedUnits.add(unit);
        }
    }

    @Override
    public BuffDef buffDefOf(String buffDefId) {
        return buffDefId == null ? null : buffDefs.get(buffDefId);
    }

    @Override
    public void onDamage(
            BattleRuntimeUnit dealer,
            BattleRuntimeUnit target,
            int dealt,
            DamageSourceKind kind,
            DamageElement element
    ) {
        applyV2Damage(dealer, target, dealt, kind, element, castingSkill, null, 1, 0, true);
    }

    /** 开战前血量快照 */
    public List<BattleUnitSnapVo> snapshotUnits() {
        List<BattleUnitSnapVo> list = new ArrayList<>();
        for (BattleRuntimeUnit u : units) {
            if (u == null) {
                continue;
            }
            BattleUnitSnapVo snap = new BattleUnitSnapVo();
            snap.setUid(u.getUnitId());
            snap.setUnitId(u.getUnitId());
            snap.setSourceId(u.getSourceId());
            snap.setName(u.getName());
            snap.setSide(u.getSide() == BattleSide.ALLY ? "ALLY" : "ENEMY");
            snap.setMaxHp(Math.max(1, u.getMaxHp()));
            snap.setHp(Math.max(0, u.getHp()));
            snap.setPosCol(u.getPosCol());
            snap.setPosRow(u.getPosRow());
            snap.setGridW(Math.max(1, u.getGridW()));
            snap.setGridH(Math.max(1, u.getGridH()));
            snap.setAtkSpeed(Math.max(1, u.getAction()));
            snap.setCode(u.getCode());
            snap.setRarity(u.getRarity());
            snap.setSkills(snapshotSkillSlots(u));
            list.add(snap);
        }
        return list;
    }

    private List<BattleSkillSlotVo> snapshotSkillSlots(BattleRuntimeUnit u) {
        List<BattleSkillSlotVo> slots = new ArrayList<>();
        if (u == null || u.getSkills() == null) {
            return slots;
        }
        // 全量技能槽（同 skillId 去重）：怪物环取各类型首条，焦点条展示全部 need>0 充能技
        Set<String> seen = new HashSet<>();
        for (ActiveSkill skill : u.getSkills()) {
            if (skill == null || skill.getSkillType() == null || skill.getId() == null) {
                continue;
            }
            ActiveSkillType type = skill.getSkillType();
            if (type != ActiveSkillType.NORMAL && type != ActiveSkillType.SMALL && type != ActiveSkillType.ULTIMATE) {
                continue;
            }
            if (!seen.add(skill.getId())) {
                continue;
            }
            BattleSkillSlotVo slot = new BattleSkillSlotVo();
            slot.setSkillId(skill.getId());
            slot.setSkillType(type.name());
            slot.setName(skill.getName());
            slot.setNeed(resolveNeedCharge(u, skill));
            int[] av = skill.getSkillType() == ActiveSkillType.NORMAL
                    ? new int[]{1, 1}
                    : firstActionValueCharge(skill.getId());
            if (av != null) {
                slot.setAvEvery(av[0]);
                slot.setAvGain(av[1]);
            }
            slots.add(slot);
        }
        return slots;
    }

    /** @return [every, gain] or null */
    private int[] firstActionValueCharge(String skillId) {
        List<SkillCharge> charges = chargesBySkill.getOrDefault(skillId, List.of());
        for (SkillCharge c : charges) {
            if (c == null || c.getConditionType() != ChargeConditionType.ACTION_VALUE) {
                continue;
            }
            ChargeScope scope = c.getScope() == null ? ChargeScope.GLOBAL : c.getScope();
            if (scope != ChargeScope.GLOBAL) {
                continue;
            }
            int every = c.getEveryActionValue() == null ? 0 : c.getEveryActionValue();
            int gain = c.getChargeGain() == null ? 0 : c.getChargeGain();
            if (every > 0 && gain > 0) {
                return new int[]{every, gain};
            }
        }
        return null;
    }

    public BattleResultVo run() {
        BattleResultVo vo = new BattleResultVo();
        emitInternal("BATTLE_START");
        runBattleStartPassives(true);
        int ticks = 0;
        while (ticks < MAX_TICKS) {
            String terminal = terminalOutcome();
            if (terminal != null) {
                vo.setOutcome(terminal);
                break;
            }
            int elapsedBefore = board.getElapsedActionValue();
            board.addElapsedAction(1);
            accrueCharges(elapsedBefore, 1);
            // 全主动技能统一走充能：同一 VA 先累计，再立即结算所有满充技能。
            flushReadySkills();
            terminal = terminalOutcome();
            if (terminal != null) {
                finishResult(vo, terminal, ticks + 1);
                return vo;
            }
            expireTimedBuffs();
            SkillV2OutputUnit.tickBuffs(this);
            terminal = terminalOutcome();
            if (terminal != null) {
                finishResult(vo, terminal, ticks + 1);
                return vo;
            }
            runBattleStartPassives(false);
            terminal = terminalOutcome();
            if (terminal != null) {
                finishResult(vo, terminal, ticks + 1);
                return vo;
            }

            notifyStatChangedInternal();
            flushReadySkills();
            terminal = terminalOutcome();
            if (terminal != null) {
                finishResult(vo, terminal, ticks + 1);
                return vo;
            }
            ticks++;
        }
        if (vo.getOutcome() == null) {
            vo.setOutcome(anyAlive(BattleSide.ENEMY) ? (anyAlive(BattleSide.ALLY) ? "DRAW" : "LOSE") : "WIN");
        }
        finishResult(vo, vo.getOutcome(), ticks);
        return vo;
    }

    /** 每个原子结算阶段结束后统一判断；同一阶段双方全灭按平局处理。 */
    private String terminalOutcome() {
        boolean allyAlive = anyAlive(BattleSide.ALLY);
        boolean enemyAlive = anyAlive(BattleSide.ENEMY);
        if (!allyAlive && !enemyAlive) {
            return "DRAW";
        }
        if (!allyAlive) {
            return "LOSE";
        }
        if (!enemyAlive) {
            return "WIN";
        }
        return null;
    }

    private void finishResult(BattleResultVo vo, String outcome, int ticks) {
        vo.setOutcome(outcome);
        vo.setTicks(ticks);
        vo.setLogs(logs);
        BattleEventVo end = emitInternal("BATTLE_END");
        end.setOutcome(outcome);
        vo.setEvents(events);
    }

    private BattleEventVo emitInternal(String type) {
        BattleEventVo e = new BattleEventVo();
        e.setT(board.getElapsedActionValue());
        e.setType(type);
        events.add(e);
        return e;
    }

    private static String shapeOf(SkillEffectTarget targetType) {
        if (targetType == SkillEffectTarget.ALL_ENEMY || targetType == SkillEffectTarget.ALL_ALLY
                || targetType == SkillEffectTarget.FRONT_ROW || targetType == SkillEffectTarget.BACK_ROW) {
            return "AOE";
        }
        return "SINGLE";
    }

    public List<String> killedEnemySourceIds() {
        List<String> ids = new ArrayList<>();
        for (BattleRuntimeUnit u : units) {
            if (u.getSide() == BattleSide.ENEMY && !u.alive() && u.getSourceId() != null) {
                ids.add(u.getSourceId());
            }
        }
        return ids;
    }

    /**
     * 统一战斗状态变更总线：凡可能影响公式的路径回调。
     */
    private void notifyStatChangedInternal() {
        if (scanningPeriodic || scanningV2 || notifyDepth > 2) {
            return;
        }
        notifyDepth++;
        try {
            scanPeriodicPassives();
            scanSustainedPassives();
            scanV2JudgeAndPulse();
        } finally {
            notifyDepth--;
        }
    }

    private void scanV2JudgeAndPulse() {
        scanningV2 = true;
        try {
            SkillV2OutputUnit.scanJudgeBuffs(this);
            scanBattleJudgePassives();
            scanBattlePulsePassives();
        } finally {
            scanningV2 = false;
        }
    }

    private void scanPeriodicPassives() {
        scanningPeriodic = true;
        try {
            Set<String> firedThisRound = new HashSet<>();
            for (BattleRuntimeUnit owner : units) {
                if (owner == null || !owner.alive()) {
                    continue;
                }
                List<PassiveSkill> passives = owner.getPeriodicPassives();
                if (passives == null || passives.isEmpty()) {
                    continue;
                }
                for (PassiveSkill p : passives) {
                    if (p == null || p.getId() == null) {
                        continue;
                    }
                    String guard = owner.getUnitId() + "#" + p.getId();
                    if (!firedThisRound.add(guard)) {
                        continue;
                    }
                    tryFirePeriodic(owner, p);
                }
            }
        } finally {
            scanningPeriodic = false;
        }
    }

    private void tryFirePeriodic(BattleRuntimeUnit owner, PassiveSkill p) {
        PeriodicTriggerMode mode = p.getPeriodicTriggerMode() == null ? PeriodicTriggerMode.SELF : p.getPeriodicTriggerMode();
        CompareOp op = p.getCompareOp() == null ? CompareOp.GTE : p.getCompareOp();
        List<BattleRuntimeUnit> candidates = resolvePeriodicCandidates(owner, mode);
        for (BattleRuntimeUnit cand : candidates) {
            if (cand == null || !cand.alive()) {
                continue;
            }
            if (!canTriggerMore(owner, p)) {
                return;
            }
            AnchorEvalContext ctx = baseEvalCtx();
            if (mode.hasSpecificTarget()) {
                ctx.setSpecificTarget(cand);
            }
            BattleRuntimeUnit formulaTarget = mode.hasSpecificTarget() ? cand : owner;
            double left = FormulaEvalUnit.eval(p.getLeftFormulaJson(), owner, formulaTarget, board, ctx);
            double right = FormulaEvalUnit.eval(p.getRightFormulaJson(), owner, formulaTarget, board, ctx);
            String candKey = mode == PeriodicTriggerMode.FORMULA ? "_FORMULA_"
                    : (mode == PeriodicTriggerMode.SELF ? owner.getUnitId() : cand.getUnitId());
            int fires = resolvePeriodicFires(owner, p.getId(), candKey, left, right, op);
            for (int i = 0; i < fires; i++) {
                if (!canTriggerMore(owner, p)) {
                    break;
                }
                bumpTriggerCount(owner, p.getId());
                int used = owner.getPeriodicTriggerCount().getOrDefault(p.getId(), 0);
                logs.add("  └ [" + owner.getName() + "] 周期被动「"
                        + (p.getName() != null ? p.getName() : p.getId()) + "」触发"
                        + (mode.hasSpecificTarget() ? "（特定目标 " + cand.getName() + "）" : "")
                        + " " + formatPeriodicTriggerCap(p, used));
                List<PassiveCombatEffect> effects = p.getCombatEffects();
                if (effects == null) {
                    continue;
                }
                for (PassiveCombatEffect e : effects) {
                    applyPeriodicEffect(owner, e, ctx);
                }
            }
        }
    }

    private List<BattleRuntimeUnit> resolvePeriodicCandidates(BattleRuntimeUnit owner, PeriodicTriggerMode mode) {
        if (owner == null || mode == null) {
            return List.of();
        }
        return switch (mode) {
            case SELF, FORMULA -> owner.alive() ? List.of(owner) : List.of();
            case ANY -> units.stream().filter(u -> u != null && u.alive()).toList();
            case ANY_ENEMY -> {
                BattleSide foe = owner.getSide() == BattleSide.ALLY ? BattleSide.ENEMY : BattleSide.ALLY;
                yield units.stream().filter(u -> u != null && u.alive() && u.getSide() == foe).toList();
            }
            case ANY_ALLY -> units.stream()
                    .filter(u -> u != null && u.alive() && u.getSide() == owner.getSide())
                    .toList();
        };
    }

    private boolean canTriggerMore(BattleRuntimeUnit owner, PassiveSkill p) {
        Integer max = p.getMaxTriggerPerBattle();
        if (max == null || max <= 0) {
            return true;
        }
        int cur = owner.getPeriodicTriggerCount().getOrDefault(p.getId(), 0);
        return cur < max;
    }

    private void bumpTriggerCount(BattleRuntimeUnit owner, String passiveId) {
        owner.getPeriodicTriggerCount().merge(passiveId, 1, Integer::sum);
    }

    private static String formatPeriodicTriggerCap(PassiveSkill p, int used) {
        Integer max = p == null ? null : p.getMaxTriggerPerBattle();
        if (max == null || max <= 0) {
            return "[不限次数]";
        }
        return "[已生效 " + used + " / " + max + "]";
    }

    /**
     * GTE/GT：阈值阶梯增量；其余：上升沿。
     * @return 本次应触发次数（通常 0 或 1，阶梯跨越多级时可 &gt;1）
     */
    private int resolvePeriodicFires(
            BattleRuntimeUnit owner,
            String passiveId,
            String candKey,
            double left,
            double right,
            CompareOp op
    ) {
        Map<String, int[]> byCand = owner.getPeriodicEdgeState()
                .computeIfAbsent(passiveId, k -> new LinkedHashMap<>());
        int[] state = byCand.computeIfAbsent(candKey, k -> new int[]{0, 0});
        if (op == CompareOp.GTE || op == CompareOp.GT) {
            double denom = Math.max(Math.abs(right), STEP_EPS);
            int step = (int) Math.floor(left / denom);
            if (step < 0) {
                step = 0;
            }
            int last = state[0];
            if (step > last) {
                int delta = step - last;
                state[0] = step;
                return delta;
            }
            state[0] = step;
            return 0;
        }
        boolean now = compare(left, op, right);
        boolean was = state[1] != 0;
        state[1] = now ? 1 : 0;
        return now && !was ? 1 : 0;
    }

    private static boolean compare(double left, CompareOp op, double right) {
        return switch (op) {
            case GT -> left > right;
            case GTE -> left >= right;
            case LT -> left < right;
            case LTE -> left <= right;
            case EQ -> Math.abs(left - right) < 1e-9;
        };
    }

    private AnchorEvalContext baseEvalCtx() {
        AnchorEvalContext ctx = new AnchorEvalContext();
        ctx.setAllUnits(units);
        return ctx;
    }

    private void accrueCharges(int elapsedBefore, int delta) {
        for (BattleRuntimeUnit u : units) {
            if (!u.alive()) {
                continue;
            }
            for (ActiveSkill skill : u.getSkills()) {
                if (skill == null || skill.getId() == null) {
                    continue;
                }
                List<SkillCharge> charges = chargesBySkill.getOrDefault(skill.getId(), List.of());
                // 普攻基础充能固定随全局 VA 1:1 增长；不再依赖单位行动条。
                int gained = skill.getSkillType() == ActiveSkillType.NORMAL ? Math.max(0, delta) : 0;
                for (SkillCharge c : charges) {
                    // 普攻的 ACTION_VALUE 基础规则由引擎保证，避免数据库重复配置导致双倍充能。
                    if (skill.getSkillType() == ActiveSkillType.NORMAL
                            && c != null && c.getConditionType() == ChargeConditionType.ACTION_VALUE) {
                        continue;
                    }
                    int gain = ChargeAccrualUnit.chargeGainedOnAdvance(c, elapsedBefore, delta);
                    gained += Math.max(0, gain);
                }
                if (gained > 0) {
                    u.addCharge(skill.getId(), gained);
                    int need = resolveNeedCharge(u, skill);
                    int cur = u.getCharge(skill.getId());
                    BattleEventVo chargeEv = emitInternal("CHARGE");
                    chargeEv.setUid(u.getUnitId());
                    chargeEv.setSkillId(skill.getId());
                    chargeEv.setSkillName(skill.getName());
                    chargeEv.setSkillType(skill.getSkillType().name());
                    chargeEv.setCur(cur);
                    chargeEv.setMax(Math.max(0, need));
                    chargeEv.setValue(gained);
                }
            }
        }
    }

    /**
     * 所有主动技能充能满后立即释放。扫描按稳定站位顺序重复进行，直到当前 VA 无满充技能。
     *
     * @return 是否释放过至少一次
     */
    private boolean flushReadySkills() {
        boolean any = false;
        List<BattleRuntimeUnit> ordered = new ArrayList<>();
        for (BattleRuntimeUnit u : units) {
            if (u != null && u.alive()) {
                ordered.add(u);
            }
        }
        ordered.sort((a, b) -> {
            int side = Integer.compare(a.getSide().ordinal(), b.getSide().ordinal());
            if (side != 0) {
                return side;
            }
            int r = Integer.compare(a.getPosRow(), b.getPosRow());
            if (r != 0) {
                return r;
            }
            return Integer.compare(a.getPosCol(), b.getPosCol());
        });
        int castsThisAxis = 0;
        while (true) {
            boolean progressed = false;
            for (BattleRuntimeUnit u : ordered) {
                if (!u.alive()) {
                    continue;
                }
                if (!anyAlive(BattleSide.ENEMY) || !anyAlive(BattleSide.ALLY)) {
                    return any;
                }
                ActiveSkill ready = pickReadySkill(u);
                if (ready == null) {
                    continue;
                }
                if (castsThisAxis >= 256) {
                    logs.add("行动值 " + board.getElapsedActionValue() + "\n满充技能连锁达到 256 次，已停止本轴后续释放");
                    return any;
                }
                castSkill(u, ready);
                castsThisAxis++;
                any = true;
                progressed = true;
            }
            if (!progressed) {
                return any;
            }
        }
    }

    private void castSkill(BattleRuntimeUnit actor, ActiveSkill skill) {
        if (actor == null || !actor.alive() || skill == null) {
            return;
        }
        // 同轴：施法结算前先清到期 BUFF，避免「不可叠加」后同帧到期断周期
        expireTimedBuffs();
        SkillV2OutputUnit.expireDueBuffs(this);
        int need = resolveNeedCharge(actor, skill);
        if (actor.getCharge(skill.getId()) < need) {
            if (need > 0) {
                logActHead(actor.getName() + " 充能不足，跳过（" + skill.getName() + "）");
            }
            return;
        }
        actor.spendCharge(skill.getId(), need);
        actor.incCast(skill.getId());
        board.recordCast(actor.getUnitId(), skill);
        logActHead(actor.getName() + " 释放「" + skill.getName() + "」");
        notifyStatChangedInternal();

        castBag = new CastDamageBag();
        castingSkill = skill;
        currentCastAffectedUnits = new LinkedHashSet<>();
        List<SkillOutput> outputs = outputsBySkill.getOrDefault(skill.getId(), List.of());
        List<SkillEffect> effects = effectsBySkill.getOrDefault(skill.getId(), List.of());
        SkillEffectTarget primaryTargetType = null;
        DamageElement castElement = DamageElement.PHYSICAL;
        if (!outputs.isEmpty()) {
            for (SkillOutput out : outputs) {
                if (out == null) {
                    continue;
                }
                if (primaryTargetType == null && out.getTargetType() != null) {
                    primaryTargetType = out.getTargetType();
                }
                if (out.getDamageElement() != null) {
                    castElement = out.getDamageElement();
                }
            }
        } else {
            for (SkillEffect effect : effects) {
                if (effect == null) {
                    continue;
                }
                if (primaryTargetType == null && effect.getTargetType() != null) {
                    primaryTargetType = effect.getTargetType();
                }
            }
        }
        BattleEventVo castEv = emitInternal("CAST");
        castEv.setUid(actor.getUnitId());
        castEv.setSkillId(skill.getId());
        castEv.setSkillName(skill.getName());
        castEv.setSkillType(skill.getSkillType() != null ? skill.getSkillType().name() : null);
        castEv.setElement(castElement.name());
        castEv.setShape(shapeOf(primaryTargetType));
        // 同步充能条：释放后剩余充能（含 0），前端据此清空再涨
        castEv.setCur(actor.getCharge(skill.getId()));
        if (need > 0) {
            castEv.setMax(need);
        }

        if (!outputs.isEmpty()) {
            // 主动技 V2 输出记为主动伤害，才能正确触发「造成/受到主动技能伤害后」
            SkillV2OutputUnit.applyOutputs(
                    this, actor, outputs, baseEvalCtx(), "  └ ", false, DamageSourceKind.ACTIVE_SKILL);
        } else {
            for (SkillEffect effect : effects) {
                applyEffect(actor, skill, effect, false);
            }
        }
        List<BattleRuntimeUnit> castHitUnits = new ArrayList<>(currentCastAffectedUnits);
        castEv.setTargets(castHitUnits.stream().map(BattleRuntimeUnit::getUnitId).toList());
        fireCombatPassives(actor, CombatEventType.AFTER_CAST_SKILL, skill, null, 0, castHitUnits);
        for (BattleRuntimeUnit t : castHitUnits) {
            fireCombatPassives(t, CombatEventType.AFTER_RECEIVE_SKILL, skill, actor, 0, List.of(actor));
        }

        if (PassiveSkillMatchUnit.isChargeSkill(skill)) {
            triggerAfterCast(actor, skill);
        }
        castBag = null;
        castingSkill = null;
        currentCastAffectedUnits = null;

        accrueSkillCharges(actor, SkillChargeEvent.CAST, skill);
        notifyStatChangedInternal();
    }

    private void logActHead(String body) {
        logs.add("行动值 " + board.getElapsedActionValue() + "\n" + body);
    }

    private void accrueSkillCharges(BattleRuntimeUnit owner, SkillChargeEvent event, ActiveSkill trigger) {
        if (owner == null || !owner.alive() || trigger == null) {
            return;
        }
        for (ActiveSkill skill : owner.getSkills()) {
            if (skill == null || skill.getId() == null) {
                continue;
            }
            List<SkillCharge> charges = chargesBySkill.getOrDefault(skill.getId(), List.of());
            int gained = 0;
            for (SkillCharge c : charges) {
                int gain = ChargeAccrualUnit.chargeGainedOnSkillEvent(c, event, trigger);
                if (gain > 0) {
                    owner.addCharge(skill.getId(), gain);
                    gained += gain;
                }
            }
            if (gained > 0) {
                int need = resolveNeedCharge(owner, skill);
                int cur = owner.getCharge(skill.getId());
                logs.add("  └ [" + owner.getName() + "] 「" + skill.getName() + "」充能 +" + gained
                        + "（" + cur + "/" + need + "）");
                BattleEventVo chargeEv = emitInternal("CHARGE");
                chargeEv.setUid(owner.getUnitId());
                chargeEv.setSkillId(skill.getId());
                chargeEv.setSkillName(skill.getName());
                chargeEv.setSkillType(skill.getSkillType() != null ? skill.getSkillType().name() : null);
                chargeEv.setCur(cur);
                chargeEv.setMax(Math.max(0, need));
                chargeEv.setValue(gained);
            }
        }
    }

    private ActiveSkill pickReadySkill(BattleRuntimeUnit actor) {
        if (actor == null) {
            return null;
        }
        for (ActiveSkill skill : actor.getSkills()) {
            if (skill == null) {
                continue;
            }
            if (!canCastByLimit(actor, skill)) {
                continue;
            }
            int need = resolveNeedCharge(actor, skill);
            if (need <= 0) {
                continue;
            }
            if (actor.getCharge(skill.getId()) >= need) {
                return skill;
            }
        }
        return null;
    }

    private boolean canCastByLimit(BattleRuntimeUnit actor, ActiveSkill skill) {
        Integer max = skill.getMaxCastSkill();
        if (max != null && max > 0 && actor.getCastCount(skill.getId()) >= max) {
            return false;
        }
        return true;
    }

    private int resolveNeedCharge(BattleRuntimeUnit actor, ActiveSkill skill) {
        if (skill.getSkillType() == ActiveSkillType.NORMAL) {
            return Math.max(1, actor.getAction());
        }
        NeedChargeMode mode = skill.getNeedChargeMode() == null ? NeedChargeMode.MANUAL : skill.getNeedChargeMode();
        if (mode == NeedChargeMode.SELF_BASE_ACTION) {
            return Math.max(0, actor.getAction());
        }
        if (mode == NeedChargeMode.FORMULA) {
            double v = FormulaEvalUnit.eval(skill.getNeedChargeFormulaJson(), actor, actor, board, baseEvalCtx());
            return Math.max(0, (int) Math.round(v));
        }
        return skill.getNeedCharge() == null ? 0 : Math.max(0, skill.getNeedCharge());
    }

    /**
     * 效果触发概率：null/≥100 必触发；≤0 永不触发；否则 nextInt(100) &lt; rate。
     */
    private static boolean rollTriggerRate(Integer triggerRate) {
        int rate = triggerRate == null ? 100 : triggerRate;
        if (rate >= 100) {
            return true;
        }
        if (rate <= 0) {
            return false;
        }
        return ThreadLocalRandom.current().nextInt(100) < rate;
    }

    private void applyEffect(BattleRuntimeUnit caster, ActiveSkill skill, SkillEffect effect, boolean fromAnchor) {
        if (effect == null || effect.getEffectType() == null) {
            return;
        }
        if (!rollTriggerRate(effect.getTriggerRate())) {
            String label = effect.getName() != null && !effect.getName().isBlank()
                    ? effect.getName() : "效果";
            int rate = effect.getTriggerRate() == null ? 100 : effect.getTriggerRate();
            logs.add("  └ 「" + label + "」未触发（概率 " + rate + "%）");
            return;
        }
        List<BattleRuntimeUnit> targets = SkillTargetResolver.resolve(effect.getTargetType(), caster, units);
        if (targets.isEmpty()) {
            logs.add("  └ 无目标（" + effect.getName() + "）");
            return;
        }
        AnchorEvalContext evalCtx = baseEvalCtx();
        int segments = effect.getHitSegments() == null || effect.getHitSegments() < 1 ? 1 : effect.getHitSegments();
        for (BattleRuntimeUnit target : targets) {
            if (!target.alive()) {
                continue;
            }
            recordAffectedTarget(target);
            for (int i = 0; i < segments; i++) {
                if (!target.alive()) {
                    break;
                }
                double raw = FormulaEvalUnit.eval(effect.getFormulaJson(), caster, target, board, evalCtx);
                int amount = (int) Math.max(0, Math.round(raw));
                if (effect.getEffectType() == SkillEffectType.DAMAGE) {
                    int dealt = resolveDealtDamage(caster, target, amount);
                    int effective = applyHpDamage(
                            caster, target, dealt, skill, effect.getTargetType(), segments, i, !fromAnchor, "  └ "
                    );
                    notifyStatChangedInternal();
                    if (!fromAnchor && PassiveSkillMatchUnit.isChargeSkill(skill)) {
                        if (castBag != null) {
                            castBag.addHit(target, effective);
                        }
                        triggerOnChargeDamage(caster, target, skill, effective);
                    }
                } else if (effect.getEffectType() == SkillEffectType.HEAL) {
                    int heal = amount;
                    target.setHp(Math.min(target.getMaxHp(), target.getHp() + heal));
                    logs.add("  └ 治疗 " + target.getName() + " " + heal
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : ""));
                    BattleEventVo healEv = emitInternal("HEAL");
                    healEv.setUid(caster.getUnitId());
                    healEv.setTarget(target.getUnitId());
                    healEv.setValue(heal);
                    healEv.setSeg(i + 1);
                    healEv.setSegTotal(segments);
                    healEv.setHpAfter(target.getHp());
                    healEv.setMaxHp(target.getMaxHp());
                    notifyStatChangedInternal();
                } else if (effect.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                    applyAttrModify(
                            target,
                            effect.getAttrKey(),
                            effect.getAttrDir(),
                            amount,
                            segments,
                            i,
                            effect.getName(),
                            buffSourceKey("SEF", effect.getId()),
                            effect.getDurationAv(),
                            false
                    );
                    notifyStatChangedInternal();
                }
            }
        }
    }

    private void triggerOnChargeDamage(BattleRuntimeUnit caster, BattleRuntimeUnit target, ActiveSkill skill, int dealt) {
        AnchorEvalContext ctx = baseEvalCtx();
        ctx.setHitDamage(dealt);
        ctx.setSkillDamage(dealt);
        ctx.setCaster(caster);
        ctx.setDamageSource(caster);
        ctx.setHitTarget(target);

        fireAnchors(caster, PassiveAnchorType.AFTER_DEAL_CHARGE_DMG, skill, ctx);
        fireAnchors(target, PassiveAnchorType.AFTER_TAKE_CHARGE_DMG, skill, ctx);
        fireAnchors(target, PassiveAnchorType.AFTER_RECEIVE_CHARGE, skill, ctx);
    }

    private void triggerAfterCast(BattleRuntimeUnit caster, ActiveSkill skill) {
        if (castBag == null) {
            return;
        }
        int total = castBag.totalDamage;
        List<BattleRuntimeUnit> hits = new ArrayList<>(castBag.hitDamageByUnit.keySet());
        if (hits.isEmpty()) {
            AnchorEvalContext ctx = baseEvalCtx();
            ctx.setHitDamage(0);
            ctx.setSkillDamage(0);
            ctx.setCaster(caster);
            ctx.setDamageSource(caster);
            fireAnchors(caster, PassiveAnchorType.AFTER_CAST_CHARGE, skill, ctx);
            return;
        }
        for (BattleRuntimeUnit hit : hits) {
            AnchorEvalContext ctx = baseEvalCtx();
            ctx.setHitDamage(castBag.hitDamageByUnit.getOrDefault(hit, 0));
            ctx.setSkillDamage(total);
            ctx.setCaster(caster);
            ctx.setDamageSource(caster);
            ctx.setHitTarget(hit);
            fireAnchors(caster, PassiveAnchorType.AFTER_CAST_CHARGE, skill, ctx);
        }
    }

    private void fireAnchors(
            BattleRuntimeUnit owner,
            PassiveAnchorType type,
            ActiveSkill trigger,
            AnchorEvalContext ctx
    ) {
        if (owner == null || !owner.alive() || type == null) {
            return;
        }
        if (ctx.getAllUnits() == null) {
            ctx.setAllUnits(units);
        }
        List<PassiveSkill> passives = owner.getAnchorPassives();
        if (passives == null || passives.isEmpty()) {
            return;
        }
        for (PassiveSkill p : passives) {
            if (p == null || p.getAnchorType() != type) {
                continue;
            }
            if (!PassiveSkillMatchUnit.matchTriggerSkill(p, trigger)) {
                continue;
            }
            if (!PassiveConditionEvalUnit.matchFormulaConditions(p, owner, board, ctx)) {
                continue;
            }
            logs.add("  └ [" + owner.getName() + "] 锚点被动「"
                    + (p.getName() != null ? p.getName() : p.getId()) + "」触发（" + type.getLabel() + "）");
            List<PassiveCombatEffect> effects = p.getCombatEffects();
            if (effects == null) {
                continue;
            }
            for (PassiveCombatEffect e : effects) {
                applyAnchorEffect(owner, e, ctx);
            }
        }
    }

    private void applyAnchorEffect(BattleRuntimeUnit owner, PassiveCombatEffect effect, AnchorEvalContext ctx) {
        applyCombatPassiveEffect(owner, effect, ctx, "    └ ");
    }

    private void applyPeriodicEffect(BattleRuntimeUnit owner, PassiveCombatEffect effect, AnchorEvalContext ctx) {
        applyCombatPassiveEffect(owner, effect, ctx, "    └ ");
    }

    /** 锚点/周期战斗效果：伤害不计主动技能次数，且不二次触发锚点伤害类 */
    private void applyCombatPassiveEffect(
            BattleRuntimeUnit owner,
            PassiveCombatEffect effect,
            AnchorEvalContext ctx,
            String logPrefix
    ) {
        if (effect == null || effect.getEffectType() == null) {
            return;
        }
        if (!rollTriggerRate(effect.getTriggerRate())) {
            String label = effect.getName() != null && !effect.getName().isBlank()
                    ? effect.getName() : "效果";
            int rate = effect.getTriggerRate() == null ? 100 : effect.getTriggerRate();
            logs.add(logPrefix + "「" + label + "」未触发（概率 " + rate + "%）");
            return;
        }
        if (ctx != null && ctx.getAllUnits() == null) {
            ctx.setAllUnits(units);
        }
        SkillEffectTarget targetType = effect.getTargetType() != null ? effect.getTargetType() : SkillEffectTarget.SELF;
        List<BattleRuntimeUnit> targets = SkillTargetResolver.resolveWithAnchor(targetType, owner, units, ctx);
        if (targets.isEmpty()) {
            logs.add(logPrefix + "无目标（" + effect.getName() + "）");
            return;
        }
        int segments = effect.getHitSegments() == null || effect.getHitSegments() < 1 ? 1 : effect.getHitSegments();
        for (BattleRuntimeUnit target : targets) {
            if (!target.alive()) {
                continue;
            }
            for (int i = 0; i < segments; i++) {
                if (!target.alive()) {
                    break;
                }
                double raw = FormulaEvalUnit.eval(effect.getFormulaJson(), owner, target, board, ctx);
                int amount = (int) Math.max(0, Math.round(raw));
                if (effect.getEffectType() == SkillEffectType.DAMAGE) {
                    int dealt = resolveDealtDamage(owner, target, amount);
                    applyHpDamage(owner, target, dealt, null, targetType, segments, i, false, logPrefix);
                } else if (effect.getEffectType() == SkillEffectType.HEAL) {
                    int heal = amount;
                    target.setHp(Math.min(target.getMaxHp(), target.getHp() + heal));
                    logs.add(logPrefix + "治疗 " + target.getName() + " " + heal
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : ""));
                    BattleEventVo healEv = emitInternal("HEAL");
                    healEv.setUid(owner.getUnitId());
                    healEv.setTarget(target.getUnitId());
                    healEv.setValue(heal);
                    healEv.setSeg(i + 1);
                    healEv.setSegTotal(segments);
                    healEv.setHpAfter(target.getHp());
                    healEv.setMaxHp(target.getMaxHp());
                } else if (effect.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                    applyAttrModify(
                            target,
                            effect.getAttrKey(),
                            effect.getAttrDir(),
                            amount,
                            segments,
                            i,
                            effect.getName(),
                            buffSourceKey("PCE", effect.getId()),
                            effect.getDurationAv(),
                            false
                    );
                }
            }
        }
    }

    private static String buffSourceKey(String prefix, String id) {
        if (id == null || id.isBlank()) {
            return prefix + "#" + System.identityHashCode(prefix);
        }
        return prefix + "#" + id;
    }

    private int resolveDealtDamage(BattleRuntimeUnit dealer, BattleRuntimeUnit target, int amount) {
        return resolveDealtDamage(dealer, target, amount, DamageElement.PHYSICAL);
    }

    private int resolveDealtDamage(
            BattleRuntimeUnit dealer, BattleRuntimeUnit target, int amount, DamageElement element
    ) {
        int raw = DamageRatioUnit.afterDefense(amount, dealer, target);
        return DamageRatioUnit.finalizeDealt(raw, dealer, target, element);
    }

    private int applyHpDamage(
            BattleRuntimeUnit dealer,
            BattleRuntimeUnit target,
            int dealt,
            ActiveSkill skill,
            SkillEffectTarget targetType,
            int segments,
            int segIdx,
            boolean recordReceive,
            String logPrefix
    ) {
        int effective = applyV2Damage(
                dealer, target, dealt,
                skill != null ? DamageSourceKind.ACTIVE_SKILL : DamageSourceKind.PASSIVE,
                DamageElement.PHYSICAL,
                skill, targetType, segments, segIdx, recordReceive
        );
        if (logPrefix != null && !logPrefix.equals("  └ ")) {
            // applyV2Damage 已写标准日志
        }
        return effective;
    }

    private int applyV2Damage(
            BattleRuntimeUnit dealer,
            BattleRuntimeUnit target,
            int dealt,
            DamageSourceKind kind,
            DamageElement element,
            ActiveSkill skill,
            SkillEffectTarget targetType,
            int segments,
            int segIdx,
            boolean recordReceive
    ) {
        if (target == null || !target.alive() || dealt <= 0) {
            return 0;
        }
        DamageElement el = element == null ? DamageElement.PHYSICAL : element;
        DamageSourceKind src = kind == null ? DamageSourceKind.ACTIVE_SKILL : kind;
        // 闪避：持有闪避效果时，被匹配技能命中可将伤害置 0
        if (skill != null && tryDodge(target, skill, dealer, dealt, el, segments, segIdx)) {
            return 0;
        }
        int hpBefore = target.getHp();
        target.setHp(Math.max(0, hpBefore - dealt));
        int effectiveDamage = hpBefore - target.getHp();
        if (dealer != null) {
            board.recordDamage(
                    dealer.getUnitId(), target.getUnitId(), effectiveDamage,
                    skill != null || src == DamageSourceKind.ACTIVE_SKILL
            );
        }
        if (recordReceive && skill != null) {
            board.recordReceive(target.getUnitId(), skill);
            accrueSkillCharges(target, SkillChargeEvent.RECEIVE, skill);
        }
        if (skill != null) {
            if (dealer != null && dealer.alive()) {
                accrueSkillCharges(dealer, SkillChargeEvent.DEAL_DAMAGE, skill);
            }
            if (target.alive()) {
                accrueSkillCharges(target, SkillChargeEvent.TAKE_DAMAGE, skill);
            }
        }
        boolean killed = hpBefore > 0 && !target.alive();
        logs.add("  └ 对 " + target.getName() + " 造成 " + effectiveDamage + " 伤害"
                + (el != DamageElement.PHYSICAL ? "（" + el.name() + "）" : "")
                + (segments > 1 ? "（第" + (segIdx + 1) + "段）" : "")
                + (killed ? "，击杀" : ""));
        BattleEventVo hitEv = emitInternal("HIT");
        hitEv.setUid(dealer != null ? dealer.getUnitId() : null);
        hitEv.setTarget(target.getUnitId());
        hitEv.setSkillId(skill != null ? skill.getId() : null);
        hitEv.setSkillName(skill != null ? skill.getName() : null);
        hitEv.setSkillType(skill != null && skill.getSkillType() != null ? skill.getSkillType().name() : null);
        hitEv.setElement(el.name());
        hitEv.setShape(shapeOf(targetType));
        hitEv.setSeg(segIdx + 1);
        hitEv.setSegTotal(Math.max(1, segments));
        hitEv.setDamage(effectiveDamage);
        hitEv.setCrit(false);
        hitEv.setHpAfter(target.getHp());
        hitEv.setMaxHp(target.getMaxHp());
        if (killed) {
            BattleEventVo deathEv = emitInternal("DEATH");
            deathEv.setUid(target.getUnitId());
            // 技能充能事件「造成击杀」：由造成击杀的技能触发匹配
            if (dealer != null && dealer.alive() && skill != null) {
                accrueSkillCharges(dealer, SkillChargeEvent.KILL, skill);
            }
        }
        applyLifeSteal(dealer, effectiveDamage);
        if (src == DamageSourceKind.ACTIVE_SKILL) {
            if (dealer != null) {
                fireCombatPassives(dealer, CombatEventType.AFTER_DEAL_ACTIVE_DMG, skill, target, effectiveDamage);
            }
            fireCombatPassives(target, CombatEventType.AFTER_TAKE_ACTIVE_DMG, skill, dealer, effectiveDamage);
        } else if (src == DamageSourceKind.PULSE_BUFF) {
            fireCombatPassives(target, CombatEventType.AFTER_TAKE_PULSE_BUFF_DMG, skill, dealer, effectiveDamage);
        }
        // 击杀/被击杀：伤害事件之后同步触发（被击杀方已死亡，仍可触发其被动）
        if (killed) {
            if (dealer != null && dealer.alive()) {
                fireCombatPassives(dealer, CombatEventType.AFTER_KILL, skill, target, effectiveDamage);
            }
            fireCombatPassives(target, CombatEventType.AFTER_KILLED, skill, dealer, effectiveDamage);
        }
        notifyStatChangedInternal();
        return effectiveDamage;
    }

    private void applyLifeSteal(BattleRuntimeUnit dealer, int dealt) {
        if (dealer == null || !dealer.alive() || dealt <= 0 || dealer.getLifeStealAdd() == 0) {
            return;
        }
        int heal = (int) Math.round(dealt * (dealer.getLifeStealAdd() / 100D));
        if (heal <= 0) {
            return;
        }
        int before = dealer.getHp();
        dealer.setHp(Math.min(dealer.getMaxHp(), before + heal));
        int got = dealer.getHp() - before;
        if (got <= 0) {
            return;
        }
        logs.add("  └ " + dealer.getName() + " 吸血恢复 " + got);
        BattleEventVo healEv = emitInternal("HEAL");
        healEv.setUid(dealer.getUnitId());
        healEv.setTarget(dealer.getUnitId());
        healEv.setValue(got);
        healEv.setHpAfter(dealer.getHp());
        healEv.setMaxHp(dealer.getMaxHp());
    }

    /**
     * 尝试闪避：持有 BuffKind.DODGE 时，匹配技能且掷骰成功则伤害为 0，并触发 AFTER_DODGE。
     * @return true 表示已闪避（调用方应中止本次伤害）
     */
    private boolean tryDodge(
            BattleRuntimeUnit target,
            ActiveSkill skill,
            BattleRuntimeUnit dealer,
            int dealt,
            DamageElement el,
            int segments,
            int segIdx
    ) {
        if (target == null || !target.alive() || skill == null) {
            return false;
        }
        List<BuffInstance> buffs = target.getBuffs();
        if (buffs == null || buffs.isEmpty()) {
            return false;
        }
        for (BuffInstance inst : buffs) {
            if (inst == null || inst.getBuffKind() != BuffKind.DODGE) {
                continue;
            }
            BuffDef def = buffDefOf(inst.getBuffDefId());
            if (def == null || def.getDodgeChance() == null || def.getDodgeChance() <= 0) {
                continue;
            }
            if (!PassiveSkillMatchUnit.matchesSkillRef(
                    def.getSkillMatchMode(),
                    def.getMatchSkillType(),
                    def.getMatchSkillSchool(),
                    def.getMatchDamageElement(),
                    def.getMatchSkillId(),
                    skill
            )) {
                continue;
            }
            int chance = Math.min(100, Math.max(0, def.getDodgeChance()));
            if (ThreadLocalRandom.current().nextInt(100) >= chance) {
                continue;
            }
            String scope = PassiveSkillMatchUnit.matchScopeLabel(
                    def.getSkillMatchMode(),
                    def.getMatchSkillType(),
                    def.getMatchSkillSchool(),
                    def.getMatchDamageElement(),
                    def.getMatchSkillId()
            );
            logs.add("  └ " + target.getName() + " 闪避成功（" + chance + "%）· " + scope
                    + (inst.getName() != null ? "「" + inst.getName() + "」" : "")
                    + "，伤害 0"
                    + (segments > 1 ? "（第" + (segIdx + 1) + "段）" : ""));
            BattleEventVo dodgeEv = emitInternal("DODGE");
            dodgeEv.setUid(target.getUnitId());
            dodgeEv.setTarget(dealer != null ? dealer.getUnitId() : null);
            dodgeEv.setSkillId(skill.getId());
            dodgeEv.setSkillName(skill.getName());
            dodgeEv.setSkillType(skill.getSkillType() != null ? skill.getSkillType().name() : null);
            dodgeEv.setElement(el != null ? el.name() : null);
            dodgeEv.setDamage(0);
            dodgeEv.setHpAfter(target.getHp());
            dodgeEv.setMaxHp(target.getMaxHp());
            dodgeEv.setValue(dealt);
            fireCombatPassives(target, CombatEventType.AFTER_DODGE, skill, dealer, 0);
            notifyStatChangedInternal();
            return true;
        }
        return false;
    }

    private void expireTimedBuffs() {
        int now = board.getElapsedActionValue();
        for (BattleRuntimeUnit u : units) {
            if (u == null) {
                continue;
            }
            List<TimedAttrBuff> list = u.getTimedBuffs();
            if (!list.isEmpty()) {
                List<TimedAttrBuff> expired = new ArrayList<>();
                for (TimedAttrBuff b : list) {
                    if (b == null || b.isSustained() || b.getExpireAtElapsed() == null) {
                        continue;
                    }
                    if (now >= b.getExpireAtElapsed()) {
                        expired.add(b);
                    }
                }
                for (TimedAttrBuff b : expired) {
                    revokeBuff(u, b, "到期");
                }
            }
        }
    }

    private void applyAttrModify(
            BattleRuntimeUnit target,
            AttrModifyKey attrKey,
            AttrModifyDirection attrDir,
            int amount,
            int segments,
            int segIdx,
            String effectName,
            String sourceKey,
            Integer durationAv,
            boolean sustained
    ) {
        if (target == null || attrKey == null || attrDir == null) {
            logs.add("  └ 属性修改未配置（" + (effectName != null ? effectName : "") + "）");
            return;
        }
        int dur = durationAv == null ? 0 : durationAv;
        boolean timed = !sustained && dur > 0;
        if ((timed || sustained) && sourceKey != null) {
            TimedAttrBuff existing = target.findBuff(sourceKey);
            if (existing != null) {
                if (timed) {
                    existing.setExpireAtElapsed(board.getElapsedActionValue() + dur);
                    logs.add("  └ " + target.getName() + " 「" + (effectName != null ? effectName : attrKey.getLabel())
                            + "」刷新持续 " + dur + " 行动值");
                    BattleEventVo buffEv = emitInternal("BUFF");
                    buffEv.setUid(target.getUnitId());
                    buffEv.setTarget(target.getUnitId());
                    buffEv.setAttrKey(attrKey.name());
                    buffEv.setDir(attrDir == AttrModifyDirection.DECREASE ? "DOWN" : "UP");
                    buffEv.setValue(Math.abs(existing.getAppliedFlat() != 0
                            ? existing.getAppliedFlat()
                            : existing.getRatioAdd()));
                    buffEv.setDuration(dur);
                    buffEv.setStack(1);
                    buffEv.setSeg(segIdx + 1);
                    buffEv.setSegTotal(segments);
                }
                return;
            }
        }

        TimedAttrBuff applied = applyAttrDelta(target, attrKey, attrDir, amount, effectName, segments, segIdx);
        if (applied == null) {
            return;
        }
        if (timed || sustained) {
            applied.setSourceKey(sourceKey);
            applied.setSustained(sustained);
            applied.setExpireAtElapsed(timed ? board.getElapsedActionValue() + dur : null);
            applied.setLabel(effectName);
            target.getTimedBuffs().add(applied);
        }
        BattleEventVo buffEv = emitInternal("BUFF");
        buffEv.setUid(target.getUnitId());
        buffEv.setTarget(target.getUnitId());
        buffEv.setAttrKey(attrKey.name());
        buffEv.setDir(attrDir == AttrModifyDirection.DECREASE ? "DOWN" : "UP");
        buffEv.setValue(Math.abs(applied.getAppliedFlat() != 0 ? applied.getAppliedFlat() : applied.getRatioAdd()));
        buffEv.setDuration(timed ? dur : 0);
        buffEv.setStack(1);
        buffEv.setSeg(segIdx + 1);
        buffEv.setSegTotal(segments);
    }

    /** @return 描述已应用变化的 buff 快照（可再挂入列表）；失败返回 null */
    private TimedAttrBuff applyAttrDelta(
            BattleRuntimeUnit target,
            AttrModifyKey attrKey,
            AttrModifyDirection attrDir,
            int amount,
            String effectName,
            int segments,
            int segIdx
    ) {
        boolean decrease = attrDir == AttrModifyDirection.DECREASE;
        int signedAmount = decrease ? -Math.abs(amount) : Math.abs(amount);
        String attrLabel = attrKey.getLabel();
        String dirLabel = attrDir.getLabel();
        TimedAttrBuff buff = new TimedAttrBuff();
        buff.setAttrKey(attrKey);
        String segPart = segments > 1 ? "（第" + (segIdx + 1) + "段）" : "";

        switch (attrKey.getModKind()) {
            case FLAT -> {
                int before;
                int after;
                switch (attrKey) {
                    case ATK -> {
                        before = target.getAtk();
                        after = Math.max(0, before + signedAmount);
                        target.setAtk(after);
                    }
                    case DEF -> {
                        before = target.getDef();
                        after = Math.max(0, before + signedAmount);
                        target.setDef(after);
                    }
                    case MAX_HP -> {
                        before = target.getMaxHp();
                        after = Math.max(1, before + signedAmount);
                        target.setMaxHp(after);
                        if (target.getHp() > after) {
                            target.setHp(after);
                        }
                    }
                    default -> {
                        return null;
                    }
                }
                buff.setAppliedFlat(after - before);
                logs.add("  └ " + target.getName() + " " + attrLabel + " " + dirLabel + " " + Math.abs(after - before)
                        + segPart + "（" + before + "→" + after + "）");
            }
            case ADD_PERCENT -> {
                buff.setRatioAdd(signedAmount);
                switch (attrKey) {
                    case LIFE_STEAL -> target.setLifeStealAdd(target.getLifeStealAdd() + signedAmount);
                    case ATK_SPEED -> {
                        target.setAtkSpeedAdd(target.getAtkSpeedAdd() + signedAmount);
                        recalcActionFromAtkSpeed(target);
                    }
                    case FINAL_ATK -> {
                        int delta = (int) Math.round(target.getAtk() * (signedAmount / 100D));
                        int before = target.getAtk();
                        int after = Math.max(0, before + delta);
                        target.setAtk(after);
                        buff.setAppliedFlat(after - before);
                        target.setFinalAtkAdd(target.getFinalAtkAdd() + signedAmount);
                    }
                    case FINAL_HP -> {
                        int delta = (int) Math.round(target.getMaxHp() * (signedAmount / 100D));
                        int before = target.getMaxHp();
                        int after = Math.max(1, before + delta);
                        target.setMaxHp(after);
                        if (delta > 0) {
                            target.setHp(target.getHp() + (after - before));
                        } else if (target.getHp() > after) {
                            target.setHp(after);
                        }
                        buff.setAppliedFlat(after - before);
                        target.setFinalHpAdd(target.getFinalHpAdd() + signedAmount);
                    }
                    case FINAL_DEF -> {
                        int delta = (int) Math.round(target.getDef() * (signedAmount / 100D));
                        int before = target.getDef();
                        int after = Math.max(0, before + delta);
                        target.setDef(after);
                        buff.setAppliedFlat(after - before);
                        target.setFinalDefAdd(target.getFinalDefAdd() + signedAmount);
                    }
                    default -> {
                        return null;
                    }
                }
                logs.add("  └ " + target.getName() + " " + attrLabel + " " + dirLabel + " " + Math.abs(signedAmount) + "%"
                        + segPart
                        + (buff.getAppliedFlat() != 0 ? "（面板 " + (buff.getAppliedFlat() > 0 ? "+" : "")
                        + buff.getAppliedFlat() + "）" : ""));
            }
            case MULT_PERCENT -> {
                if (!DamageRatioUnit.isDamageRatioKey(attrKey)) {
                    return null;
                }
                double factor = DamageRatioUnit.factorFromSignedPercent(signedAmount);
                buff.setRatioFactor(factor);
                buff.setRatioAdd(signedAmount);
                DamageRatioUnit.applyMultFactor(target, attrKey, factor);
                logs.add("  └ " + target.getName() + " " + attrLabel + " " + dirLabel + " " + Math.abs(signedAmount) + "%"
                        + segPart);
            }
            default -> {
                return null;
            }
        }
        if (effectName != null && !effectName.isBlank() && logs.size() > 0) {
            // keep log as-is; effect name optional in refresh path
        }
        return buff;
    }

    private void revokeBuff(BattleRuntimeUnit target, TimedAttrBuff buff, String reason) {
        if (target == null || buff == null || buff.getAttrKey() == null) {
            return;
        }
        AttrModifyKey attrKey = buff.getAttrKey();
        switch (attrKey.getModKind()) {
            case FLAT -> {
                int flat = buff.getAppliedFlat();
                if (flat != 0) {
                    switch (attrKey) {
                        case ATK -> target.setAtk(Math.max(0, target.getAtk() - flat));
                        case DEF -> target.setDef(Math.max(0, target.getDef() - flat));
                        case MAX_HP -> {
                            int after = Math.max(1, target.getMaxHp() - flat);
                            target.setMaxHp(after);
                            if (target.getHp() > after) {
                                target.setHp(after);
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
            case ADD_PERCENT -> {
                int add = buff.getRatioAdd();
                switch (attrKey) {
                    case LIFE_STEAL -> target.setLifeStealAdd(target.getLifeStealAdd() - add);
                    case ATK_SPEED -> {
                        target.setAtkSpeedAdd(target.getAtkSpeedAdd() - add);
                        recalcActionFromAtkSpeed(target);
                    }
                    case FINAL_ATK -> {
                        target.setAtk(Math.max(0, target.getAtk() - buff.getAppliedFlat()));
                        target.setFinalAtkAdd(target.getFinalAtkAdd() - add);
                    }
                    case FINAL_HP -> {
                        int after = Math.max(1, target.getMaxHp() - buff.getAppliedFlat());
                        target.setMaxHp(after);
                        if (target.getHp() > after) {
                            target.setHp(after);
                        }
                        target.setFinalHpAdd(target.getFinalHpAdd() - add);
                    }
                    case FINAL_DEF -> {
                        target.setDef(Math.max(0, target.getDef() - buff.getAppliedFlat()));
                        target.setFinalDefAdd(target.getFinalDefAdd() - add);
                    }
                    default -> {
                    }
                }
            }
            case MULT_PERCENT -> {
                double factor = buff.getRatioFactor();
                if (factor != 0D && DamageRatioUnit.isDamageRatioKey(attrKey)) {
                    DamageRatioUnit.revokeMultFactor(target, attrKey, factor);
                }
            }
            default -> {
            }
        }
        target.getTimedBuffs().remove(buff);
        String label = buff.getLabel() != null ? buff.getLabel() : attrKey.getLabel();
        logs.add("  └ " + target.getName() + " 「" + label + "」效果结束（" + reason + "）");
        BattleEventVo endEv = emitInternal("BUFF_END");
        endEv.setUid(target.getUnitId());
        endEv.setTarget(target.getUnitId());
        endEv.setAttrKey(attrKey.name());
        endEv.setDuration(0);
        endEv.setValue(Math.abs(buff.getAppliedFlat() != 0 ? buff.getAppliedFlat() : buff.getRatioAdd()));
    }

    private void recalcActionFromAtkSpeed(BattleRuntimeUnit unit) {
        if (unit == null) {
            return;
        }
        int base = unit.getBaseAction() > 0 ? unit.getBaseAction() : Math.max(1, unit.getAction());
        int add = unit.getAtkSpeedAdd();
        List<Integer> ups = new ArrayList<>();
        List<Integer> downs = new ArrayList<>();
        if (add > 0) {
            ups.add(add);
        } else if (add < 0) {
            downs.add(-add);
        }
        int newAction = AtkSpeedCalcUnit.calcFinalAction(base, ups, downs);
        unit.setAction(newAction);
    }

    private void scanSustainedPassives() {
        if (scanningSustained) {
            return;
        }
        scanningSustained = true;
        try {
            for (BattleRuntimeUnit owner : units) {
                if (owner == null || !owner.alive()) {
                    continue;
                }
                List<PassiveSkill> passives = owner.getSustainedPassives();
                if (passives == null || passives.isEmpty()) {
                    continue;
                }
                for (PassiveSkill p : passives) {
                    if (p == null || p.getId() == null) {
                        continue;
                    }
                    syncSustainedPassive(owner, p);
                }
            }
        } finally {
            scanningSustained = false;
        }
    }

    private void syncSustainedPassive(BattleRuntimeUnit owner, PassiveSkill p) {
        PeriodicTriggerMode mode = p.getPeriodicTriggerMode() == null ? PeriodicTriggerMode.SELF : p.getPeriodicTriggerMode();
        CompareOp op = p.getCompareOp() == null ? CompareOp.GTE : p.getCompareOp();
        List<BattleRuntimeUnit> candidates = resolvePeriodicCandidates(owner, mode);
        boolean conditionMet = false;
        AnchorEvalContext ctx = baseEvalCtx();
        for (BattleRuntimeUnit cand : candidates) {
            if (cand == null || !cand.alive()) {
                continue;
            }
            if (mode.hasSpecificTarget()) {
                ctx.setSpecificTarget(cand);
            }
            BattleRuntimeUnit formulaTarget = mode.hasSpecificTarget() ? cand : owner;
            double left = FormulaEvalUnit.eval(p.getLeftFormulaJson(), owner, formulaTarget, board, ctx);
            double right = FormulaEvalUnit.eval(p.getRightFormulaJson(), owner, formulaTarget, board, ctx);
            if (compare(left, op, right)) {
                conditionMet = true;
                break;
            }
        }
        boolean active = owner.getSustainedActiveIds().contains(p.getId());
        if (conditionMet && !active) {
            activateSustained(owner, p, ctx);
        } else if (!conditionMet && active) {
            deactivateSustained(owner, p);
        }
    }

    private void activateSustained(BattleRuntimeUnit owner, PassiveSkill p, AnchorEvalContext ctx) {
        List<PassiveCombatEffect> effects = p.getCombatEffects();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        logs.add("  └ [" + owner.getName() + "] 持续效果「"
                + (p.getName() != null ? p.getName() : p.getId()) + "」生效");
        boolean any = false;
        for (PassiveCombatEffect e : effects) {
            if (e == null || e.getEffectType() != SkillEffectType.ATTR_MODIFY) {
                continue;
            }
            if (!rollTriggerRate(e.getTriggerRate())) {
                continue;
            }
            SkillEffectTarget targetType = e.getTargetType() != null ? e.getTargetType() : SkillEffectTarget.SELF;
            List<BattleRuntimeUnit> targets = SkillTargetResolver.resolveWithAnchor(targetType, owner, units, ctx);
            for (BattleRuntimeUnit target : targets) {
                if (target == null || !target.alive()) {
                    continue;
                }
                double raw = FormulaEvalUnit.eval(e.getFormulaJson(), owner, target, board, ctx);
                int amount = (int) Math.max(0, Math.round(raw));
                applyAttrModify(
                        target,
                        e.getAttrKey(),
                        e.getAttrDir(),
                        amount,
                        1,
                        0,
                        e.getName(),
                        sustainedBuffKey(p.getId(), e.getId()),
                        null,
                        true
                );
                any = true;
            }
        }
        if (any) {
            owner.getSustainedActiveIds().add(p.getId());
        }
    }

    private void deactivateSustained(BattleRuntimeUnit owner, PassiveSkill p) {
        String prefix = "SUS#" + p.getId() + "#";
        for (BattleRuntimeUnit u : units) {
            if (u == null) {
                continue;
            }
            List<TimedAttrBuff> copy = new ArrayList<>(u.getTimedBuffs());
            for (TimedAttrBuff b : copy) {
                if (b != null && b.getSourceKey() != null && b.getSourceKey().startsWith(prefix)) {
                    revokeBuff(u, b, "条件解除");
                }
            }
        }
        owner.getSustainedActiveIds().remove(p.getId());
        logs.add("  └ [" + owner.getName() + "] 持续效果「"
                + (p.getName() != null ? p.getName() : p.getId()) + "」取消");
    }

    private static String sustainedBuffKey(String passiveId, String effectId) {
        return "SUS#" + passiveId + "#" + (effectId != null ? effectId : "0");
    }

    private BattleRuntimeUnit findUnitById(String unitId) {
        if (unitId == null) {
            return null;
        }
        for (BattleRuntimeUnit u : units) {
            if (u != null && unitId.equals(u.getUnitId())) {
                return u;
            }
        }
        return null;
    }

    private void runBattleStartPassives(boolean immediateOnly) {
        int now = board.getElapsedActionValue();
        for (BattleRuntimeUnit owner : units) {
            if (owner == null || !owner.alive()) {
                continue;
            }
            List<PassiveSkill> list = owner.getBattleStartPassives();
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (PassiveSkill p : list) {
                if (p == null || p.getId() == null) {
                    continue;
                }
                BattleStartApplyRule rule = p.getStartApplyRule() == null
                        ? BattleStartApplyRule.IMMEDIATE : p.getStartApplyRule();
                int every = p.getStartElapsedAv() == null ? 0 : Math.max(0, p.getStartElapsedAv());
                if (rule == BattleStartApplyRule.IMMEDIATE) {
                    if (!immediateOnly) {
                        continue;
                    }
                    if (owner.getStartRuleState().containsKey(p.getId())) {
                        continue;
                    }
                    owner.getStartRuleState().put(p.getId(), 0);
                    fireV2Passive(owner, p, baseEvalCtx(), "开战");
                } else if (rule == BattleStartApplyRule.AT_ELAPSED_ONCE) {
                    if (immediateOnly || every <= 0 || now < every) {
                        continue;
                    }
                    if (owner.getStartRuleState().containsKey(p.getId())) {
                        continue;
                    }
                    owner.getStartRuleState().put(p.getId(), now);
                    fireV2Passive(owner, p, baseEvalCtx(), "开战延时");
                } else if (rule == BattleStartApplyRule.EVERY_ELAPSED) {
                    if (immediateOnly || every <= 0) {
                        continue;
                    }
                    int step = now / every;
                    int last = owner.getStartRuleState().getOrDefault(p.getId(), 0);
                    if (step > last) {
                        owner.getStartRuleState().put(p.getId(), step);
                        for (int i = last; i < step; i++) {
                            fireV2Passive(owner, p, baseEvalCtx(), "开战脉冲");
                        }
                    }
                }
            }
        }
    }

    private void scanBattleJudgePassives() {
        for (BattleRuntimeUnit owner : units) {
            if (owner == null || !owner.alive()) {
                continue;
            }
            List<PassiveSkill> list = owner.getBattleJudgePassives();
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (PassiveSkill p : list) {
                if (p == null || p.getId() == null) {
                    continue;
                }
                AnchorEvalContext ctx = baseEvalCtx();
                if (!PassiveConditionEvalUnit.matchFormulaConditions(p, owner, board, ctx)) {
                    if (owner.getJudgeActiveIds().remove(p.getId())) {
                        revokeJudgeOutputs(owner, p);
                    }
                    continue;
                }
                double left = FormulaEvalUnit.eval(p.getLeftFormulaJson(), owner, owner, board, ctx);
                double right = FormulaEvalUnit.eval(p.getRightFormulaJson(), owner, owner, board, ctx);
                CompareOp op = p.getCompareOp() == null ? CompareOp.GTE : p.getCompareOp();
                boolean ok = compareJudge(left, op, right);
                if (ok) {
                    if (owner.getJudgeActiveIds().add(p.getId())) {
                        fireV2Passive(owner, p, ctx, "判定", true);
                    }
                } else if (owner.getJudgeActiveIds().remove(p.getId())) {
                    revokeJudgeOutputs(owner, p);
                }
            }
        }
    }

    private void scanBattlePulsePassives() {
        for (BattleRuntimeUnit owner : units) {
            if (owner == null || !owner.alive()) {
                continue;
            }
            List<PassiveSkill> list = owner.getBattlePulsePassives();
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (PassiveSkill p : list) {
                if (p == null || p.getId() == null || !canTriggerMore(owner, p)) {
                    continue;
                }
                AnchorEvalContext ctx = baseEvalCtx();
                if (!PassiveConditionEvalUnit.matchFormulaConditions(p, owner, board, ctx)) {
                    continue;
                }
                double left = FormulaEvalUnit.eval(p.getLeftFormulaJson(), owner, owner, board, ctx);
                double right = FormulaEvalUnit.eval(p.getRightFormulaJson(), owner, owner, board, ctx);
                // 脉冲固定「每当达到」阈值阶梯
                int fires = resolvePeriodicFires(owner, p.getId(), owner.getUnitId(), left, right, CompareOp.GTE);
                for (int i = 0; i < fires; i++) {
                    if (!canTriggerMore(owner, p)) {
                        break;
                    }
                    bumpTriggerCount(owner, p.getId());
                    fireV2Passive(owner, p, ctx, "脉冲", false);
                }
            }
        }
    }

    private void fireCombatPassives(
            BattleRuntimeUnit owner,
            CombatEventType event,
            ActiveSkill skill,
            BattleRuntimeUnit other,
            int dealt
    ) {
        List<BattleRuntimeUnit> hits = null;
        if (other != null) {
            hits = List.of(other);
        }
        fireCombatPassives(owner, event, skill, other, dealt, hits);
    }

    private void fireCombatPassives(
            BattleRuntimeUnit owner,
            CombatEventType event,
            ActiveSkill skill,
            BattleRuntimeUnit other,
            int dealt,
            List<BattleRuntimeUnit> hitTargets
    ) {
        if (owner == null || event == null) {
            return;
        }
        boolean allowDeadOwner = event == CombatEventType.AFTER_KILLED;
        if (!allowDeadOwner && !owner.alive()) {
            return;
        }
        List<PassiveSkill> list = owner.getBattleCombatPassives();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (PassiveSkill p : list) {
            if (p == null || p.getCombatEvent() != event) {
                continue;
            }
            if (!canTriggerMore(owner, p)) {
                continue;
            }
            if (skill != null && !PassiveSkillMatchUnit.matchesSkillRef(p, skill)) {
                continue;
            }
            AnchorEvalContext ctx = baseEvalCtx();
            boolean receiveSide = event == CombatEventType.AFTER_RECEIVE_SKILL
                    || event == CombatEventType.AFTER_TAKE_ACTIVE_DMG
                    || event == CombatEventType.AFTER_TAKE_PULSE_BUFF_DMG
                    || event == CombatEventType.AFTER_KILLED
                    || event == CombatEventType.AFTER_DODGE;
            boolean killSide = event == CombatEventType.AFTER_KILL || event == CombatEventType.AFTER_KILLED;
            ctx.setCaster(receiveSide ? other : owner);
            ctx.setHitDamage(dealt);
            ctx.setSkillDamage(dealt);
            List<BattleRuntimeUnit> hits = hitTargets;
            if ((hits == null || hits.isEmpty()) && other != null) {
                hits = List.of(other);
            }
            if (hits != null && !hits.isEmpty()) {
                ctx.setHitTargets(hits);
                ctx.setHitTarget(hits.get(0));
            }
            if (receiveSide) {
                ctx.setDamageSource(other != null ? other : owner);
            } else {
                ctx.setDamageSource(owner);
            }
            if (killSide) {
                BattleRuntimeUnit killer = event == CombatEventType.AFTER_KILL ? owner : other;
                BattleRuntimeUnit killedUnit = event == CombatEventType.AFTER_KILL ? other : owner;
                ctx.setKiller(killer);
                ctx.setKilled(killedUnit);
                if (killedUnit != null) {
                    ctx.setHitTarget(killedUnit);
                    ctx.setHitTargets(List.of(killedUnit));
                }
                if (killer != null) {
                    ctx.setDamageSource(killer);
                    ctx.setCaster(killer);
                }
            }
            if (!PassiveConditionEvalUnit.matchFormulaConditions(p, owner, board, ctx)) {
                continue;
            }
            String triggerKey = owner.getUnitId() + "#" + p.getId();
            if (combatTriggerDepth >= MAX_COMBAT_TRIGGER_DEPTH || !activeCombatTriggerKeys.add(triggerKey)) {
                logs.add("  └ [" + owner.getName() + "] 战斗事件「"
                        + (p.getName() != null ? p.getName() : p.getId()) + "」跳过：触发链循环");
                continue;
            }
            bumpTriggerCount(owner, p.getId());
            BattleEventVo triggerEv = emitInternal("PASSIVE_TRIGGER");
            triggerEv.setUid(owner.getUnitId());
            triggerEv.setSkillId(p.getId());
            triggerEv.setSkillName(p.getName());
            triggerEv.setSkillType("PASSIVE");
            triggerEv.setTriggerEvent(event.name());
            triggerEv.setTarget(other != null ? other.getUnitId() : null);
            triggerEv.setDamage(dealt);
            combatTriggerDepth++;
            try {
                fireV2Passive(owner, p, ctx, "战斗事件", false);
            } finally {
                combatTriggerDepth--;
                activeCombatTriggerKeys.remove(triggerKey);
            }
        }
    }

    private void fireV2Passive(BattleRuntimeUnit owner, PassiveSkill p, AnchorEvalContext ctx, String tag) {
        fireV2Passive(owner, p, ctx, tag, false);
    }

    private void fireV2Passive(
            BattleRuntimeUnit owner, PassiveSkill p, AnchorEvalContext ctx, String tag, boolean revokeableAttr
    ) {
        if (owner == null || p == null) {
            return;
        }
        List<SkillOutput> outs = p.getOutputs();
        String skillName = p.getName() != null ? p.getName() : p.getId();
        boolean startAxis = "开战".equals(tag) || "开战延时".equals(tag) || "开战脉冲".equals(tag);
        if (outs == null || outs.isEmpty()) {
            if (startAxis) {
                logActHead(owner.getName() + " 释放「" + skillName + "」（无输出）");
            } else {
                logs.add("  └ [" + owner.getName() + "] " + tag + "「" + skillName + "」跳过：无输出配置");
            }
            return;
        }
        if (startAxis) {
            // 主轴时间：与主动技同一套「释放」日志 + CAST 事件
            logActHead(owner.getName() + " 释放「" + skillName + "」");
            BattleEventVo castEv = emitInternal("CAST");
            castEv.setUid(owner.getUnitId());
            castEv.setSkillId(p.getId());
            castEv.setSkillName(skillName);
            castEv.setSkillType("PASSIVE");
            LinkedHashSet<String> castTargets = new LinkedHashSet<>();
            for (SkillOutput out : outs) {
                if (out == null || out.getTargetType() == null) {
                    continue;
                }
                for (BattleRuntimeUnit t : SkillTargetResolver.resolveWithAnchor(
                        out.getTargetType(), owner, units, ctx != null ? ctx : baseEvalCtx())) {
                    if (t != null && t.getUnitId() != null) {
                        castTargets.add(t.getUnitId());
                    }
                }
            }
            castEv.setTargets(new ArrayList<>(castTargets));
        } else {
            logs.add("行动值 " + board.getElapsedActionValue() + "\n  └ [" + owner.getName() + "] " + tag + "「"
                    + skillName + "」触发");
        }
        // 被动是独立结算源，不能继承外层主动技能，也不能把被动目标写进外层 CAST.targets。
        ActiveSkill previousSkill = castingSkill;
        LinkedHashSet<BattleRuntimeUnit> previousAffected = currentCastAffectedUnits;
        castingSkill = null;
        currentCastAffectedUnits = null;
        try {
            SkillV2OutputUnit.applyOutputs(this, owner, outs, ctx, "  └ ", revokeableAttr);
        } finally {
            castingSkill = previousSkill;
            currentCastAffectedUnits = previousAffected;
        }
    }

    private void revokeJudgeOutputs(BattleRuntimeUnit owner, PassiveSkill p) {
        if (p == null || p.getOutputs() == null) {
            return;
        }
        for (SkillOutput out : p.getOutputs()) {
            if (out == null || out.getId() == null) {
                continue;
            }
            String prefix = "SOUT#" + out.getId() + "#";
            for (BattleRuntimeUnit u : units) {
                if (u == null) {
                    continue;
                }
                List<TimedAttrBuff> copy = new ArrayList<>(u.getTimedBuffs());
                for (TimedAttrBuff b : copy) {
                    if (b != null && b.getSourceKey() != null && b.getSourceKey().startsWith(prefix)) {
                        revokeBuff(u, b, "判定解除");
                    }
                }
            }
        }
        logs.add("  └ [" + owner.getName() + "] 判定「"
                + (p.getName() != null ? p.getName() : p.getId()) + "」取消");
    }

    private static boolean compareJudge(double left, CompareOp op, double right) {
        if (op == null) {
            return false;
        }
        return switch (op) {
            case GT -> left > right;
            case GTE -> left >= right;
            case LT -> left < right;
            case LTE -> left <= right;
            case EQ -> Math.abs(left - right) < 1e-6;
        };
    }

    private boolean anyAlive(BattleSide side) {
        for (BattleRuntimeUnit u : units) {
            if (u.getSide() == side && u.alive()) {
                return true;
            }
        }
        return false;
    }

    private static final class CastDamageBag {
        private int totalDamage;
        private final Map<BattleRuntimeUnit, Integer> hitDamageByUnit = new LinkedHashMap<>();

        void addHit(BattleRuntimeUnit target, int dealt) {
            if (target == null || dealt <= 0) {
                return;
            }
            totalDamage += dealt;
            hitDamageByUnit.merge(target, dealt, Integer::sum);
        }
    }
}
