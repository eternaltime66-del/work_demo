package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveCombatEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PeriodicTriggerMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleUnitSnapVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 行动值战斗演算：
 * <ul>
 *   <li>每 tick 全局已经过行动值 +1，各存活单位行动进度 +1</li>
 *   <li>进度达到自身行动阈值则行动：释放可用技能，结算效果</li>
 *   <li>敌方全灭胜利 / 我方全灭失败</li>
 *   <li>充能技能路径触发锚点被动；状态变更总线触发周期被动（不递归伤害类）</li>
 * </ul>
 */
public class BattleEngine {

    public static final int MAX_TICKS = 5000;
    private static final double STEP_EPS = 1e-9;

    private final List<BattleRuntimeUnit> units = new ArrayList<>();
    private final Map<String, List<SkillCharge>> chargesBySkill = new HashMap<>();
    private final Map<String, List<SkillEffect>> effectsBySkill = new HashMap<>();
    private final BattleStatBoard board = new BattleStatBoard();
    private final List<String> logs = new ArrayList<>();

    /** 当前施法累计伤害统计（castSkill 期间） */
    private CastDamageBag castBag;

    /** 周期扫描防重入 */
    private boolean scanningPeriodic;
    private int notifyDepth;

    public void addUnit(BattleRuntimeUnit unit) {
        if (unit != null) {
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

    public void addLog(String line) {
        if (line != null && !line.isBlank()) {
            logs.add(line);
        }
    }

    /** 开战前血量快照 */
    public List<BattleUnitSnapVo> snapshotUnits() {
        List<BattleUnitSnapVo> list = new ArrayList<>();
        for (BattleRuntimeUnit u : units) {
            if (u == null) {
                continue;
            }
            BattleUnitSnapVo snap = new BattleUnitSnapVo();
            snap.setUnitId(u.getUnitId());
            snap.setSourceId(u.getSourceId());
            snap.setName(u.getName());
            snap.setSide(u.getSide() == BattleSide.ALLY ? "ALLY" : "ENEMY");
            snap.setMaxHp(Math.max(1, u.getMaxHp()));
            snap.setHp(Math.max(0, u.getHp()));
            list.add(snap);
        }
        return list;
    }

    public BattleResultVo run() {
        BattleResultVo vo = new BattleResultVo();
        int ticks = 0;
        while (ticks < MAX_TICKS) {
            if (!anyAlive(BattleSide.ALLY)) {
                vo.setOutcome("LOSE");
                break;
            }
            if (!anyAlive(BattleSide.ENEMY)) {
                vo.setOutcome("WIN");
                break;
            }
            int elapsedBefore = board.getElapsedActionValue();
            board.addElapsedAction(1);
            accrueCharges(elapsedBefore, 1);

            List<BattleRuntimeUnit> actors = new ArrayList<>();
            for (BattleRuntimeUnit u : units) {
                if (!u.alive()) {
                    continue;
                }
                u.setActionProgress(u.getActionProgress() + 1);
                int need = Math.max(1, u.getAction());
                if (u.getActionProgress() >= need) {
                    u.setActionProgress(0);
                    actors.add(u);
                }
            }
            notifyStatChanged();
            actors.sort((a, b) -> {
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
            for (BattleRuntimeUnit actor : actors) {
                if (!actor.alive()) {
                    continue;
                }
                act(actor);
                if (!anyAlive(BattleSide.ALLY)) {
                    vo.setOutcome("LOSE");
                    ticks++;
                    vo.setTicks(ticks);
                    vo.setLogs(logs);
                    return vo;
                }
                if (!anyAlive(BattleSide.ENEMY)) {
                    vo.setOutcome("WIN");
                    ticks++;
                    vo.setTicks(ticks);
                    vo.setLogs(logs);
                    return vo;
                }
            }
            ticks++;
        }
        if (vo.getOutcome() == null) {
            vo.setOutcome(anyAlive(BattleSide.ENEMY) ? (anyAlive(BattleSide.ALLY) ? "DRAW" : "LOSE") : "WIN");
        }
        vo.setTicks(ticks);
        vo.setLogs(logs);
        return vo;
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
    private void notifyStatChanged() {
        if (scanningPeriodic || notifyDepth > 2) {
            return;
        }
        notifyDepth++;
        try {
            scanPeriodicPassives();
        } finally {
            notifyDepth--;
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
        boolean gained = false;
        for (BattleRuntimeUnit u : units) {
            if (!u.alive()) {
                continue;
            }
            for (ActiveSkill skill : u.getSkills()) {
                List<SkillCharge> charges = chargesBySkill.getOrDefault(skill.getId(), List.of());
                for (SkillCharge c : charges) {
                    int gain = ChargeAccrualUnit.chargeGainedOnAdvance(c, elapsedBefore, delta);
                    if (gain > 0) {
                        u.addCharge(skill.getId(), gain);
                        gained = true;
                    }
                }
            }
        }
        if (gained) {
            // tick 末尾统一 notify；此处不重复
        }
    }

    private void act(BattleRuntimeUnit actor) {
        ActiveSkill skill = pickSkill(actor);
        if (skill == null) {
            logActHead(actor.getName() + " 行动但无可释放技能");
            return;
        }
        castSkill(actor, skill);
        int guard = 0;
        while (actor.alive() && guard++ < 8) {
            if (!anyAlive(BattleSide.ENEMY) || !anyAlive(BattleSide.ALLY)) {
                break;
            }
            ActiveSkill chained = pickReadyChargedSkill(actor);
            if (chained == null) {
                break;
            }
            castSkill(actor, chained);
        }
    }

    private void castSkill(BattleRuntimeUnit actor, ActiveSkill skill) {
        if (actor == null || !actor.alive() || skill == null) {
            return;
        }
        int need = resolveNeedCharge(actor, skill);
        if (actor.getCharge(skill.getId()) < need) {
            if (need > 0) {
                logActHead(actor.getName() + " 充能不足，跳过（" + skill.getName() + "）");
            }
            return;
        }
        actor.spendCharge(skill.getId(), need);
        actor.incCast(skill.getId());
        board.recordCast(actor.getUnitId(), skill.getId(), skill.getSkillType());
        logActHead(actor.getName() + " 释放「" + skill.getName() + "」");
        notifyStatChanged();

        castBag = new CastDamageBag();
        List<SkillEffect> effects = effectsBySkill.getOrDefault(skill.getId(), List.of());
        for (SkillEffect effect : effects) {
            applyEffect(actor, skill, effect, false);
        }

        if (PassiveSkillMatchUnit.isChargeSkill(skill)) {
            triggerAfterCast(actor, skill);
        }
        castBag = null;

        accrueSkillCharges(actor, SkillChargeEvent.CAST, skill);
        notifyStatChanged();
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
            if (skill.getSkillType() == ActiveSkillType.NORMAL) {
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
                logs.add("  └ 「" + skill.getName() + "」充能 +" + gained
                        + "（" + cur + "/" + need + "）");
            }
        }
    }

    private ActiveSkill pickSkill(BattleRuntimeUnit actor) {
        ActiveSkill normal = null;
        ActiveSkill charged = null;
        for (ActiveSkill skill : actor.getSkills()) {
            if (skill == null || !canCastByLimit(actor, skill)) {
                continue;
            }
            int need = resolveNeedCharge(actor, skill);
            boolean ready = need <= 0 || actor.getCharge(skill.getId()) >= need;
            if (!ready) {
                continue;
            }
            if (skill.getSkillType() == ActiveSkillType.NORMAL) {
                normal = skill;
            } else if (charged == null) {
                charged = skill;
            }
        }
        return charged != null ? charged : normal;
    }

    private ActiveSkill pickReadyChargedSkill(BattleRuntimeUnit actor) {
        if (actor == null) {
            return null;
        }
        for (ActiveSkill skill : actor.getSkills()) {
            if (skill == null || skill.getSkillType() == ActiveSkillType.NORMAL) {
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
        NeedChargeMode mode = skill.getNeedChargeMode() == null ? NeedChargeMode.MANUAL : skill.getNeedChargeMode();
        if (mode == NeedChargeMode.SELF_BASE_ACTION) {
            return Math.max(0, actor.getAction());
        }
        return skill.getNeedCharge() == null ? 0 : Math.max(0, skill.getNeedCharge());
    }

    private void applyEffect(BattleRuntimeUnit caster, ActiveSkill skill, SkillEffect effect, boolean fromAnchor) {
        if (effect == null || effect.getEffectType() == null) {
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
            for (int i = 0; i < segments; i++) {
                double raw = FormulaEvalUnit.eval(effect.getFormulaJson(), caster, target, board, evalCtx);
                int amount = (int) Math.max(0, Math.round(raw));
                if (effect.getEffectType() == SkillEffectType.DAMAGE) {
                    int dealt = Math.max(1, amount - Math.max(0, target.getDef() / 10));
                    target.setHp(Math.max(0, target.getHp() - dealt));
                    board.recordDamage(caster.getUnitId(), target.getUnitId(), dealt, !fromAnchor);
                    board.recordReceive(target.getUnitId(), skill.getId(), skill.getSkillType());
                    if (!fromAnchor) {
                        accrueSkillCharges(target, SkillChargeEvent.RECEIVE, skill);
                    }
                    logs.add("  └ 对 " + target.getName() + " 造成 " + dealt + " 伤害"
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : "")
                            + (target.alive() ? "" : "，击杀"));
                    notifyStatChanged();
                    if (!fromAnchor && PassiveSkillMatchUnit.isChargeSkill(skill)) {
                        if (castBag != null) {
                            castBag.addHit(target, dealt);
                        }
                        triggerOnChargeDamage(caster, target, skill, dealt);
                    }
                } else if (effect.getEffectType() == SkillEffectType.HEAL) {
                    int heal = amount;
                    target.setHp(Math.min(target.getMaxHp(), target.getHp() + heal));
                    logs.add("  └ 治疗 " + target.getName() + " " + heal
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : ""));
                    notifyStatChanged();
                } else if (effect.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                    applyAttrModify(target, effect.getAttrKey(), effect.getAttrDir(), amount, segments, i, effect.getName());
                    notifyStatChanged();
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
                double raw = FormulaEvalUnit.eval(effect.getFormulaJson(), owner, target, board, ctx);
                int amount = (int) Math.max(0, Math.round(raw));
                if (effect.getEffectType() == SkillEffectType.DAMAGE) {
                    int dealt = Math.max(1, amount - Math.max(0, target.getDef() / 10));
                    target.setHp(Math.max(0, target.getHp() - dealt));
                    board.recordDamage(owner.getUnitId(), target.getUnitId(), dealt, false);
                    logs.add(logPrefix + "对 " + target.getName() + " 造成 " + dealt + " 伤害"
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : "")
                            + (target.alive() ? "" : "，击杀"));
                } else if (effect.getEffectType() == SkillEffectType.HEAL) {
                    int heal = amount;
                    target.setHp(Math.min(target.getMaxHp(), target.getHp() + heal));
                    logs.add(logPrefix + "治疗 " + target.getName() + " " + heal
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : ""));
                } else if (effect.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                    applyAttrModify(target, effect.getAttrKey(), effect.getAttrDir(), amount, segments, i, effect.getName());
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
            String effectName
    ) {
        if (attrKey == null || attrDir == null) {
            logs.add("  └ 属性修改未配置（" + (effectName != null ? effectName : "") + "）");
            return;
        }
        boolean decrease = attrDir == AttrModifyDirection.DECREASE;
        int delta = decrease ? -amount : amount;
        String attrLabel = attrKey.getLabel();
        String dirLabel = attrDir.getLabel();
        int before;
        int after;
        switch (attrKey) {
            case ATK -> {
                before = target.getAtk();
                after = Math.max(0, before + delta);
                target.setAtk(after);
            }
            case DEF -> {
                before = target.getDef();
                after = Math.max(0, before + delta);
                target.setDef(after);
            }
            case MAX_HP -> {
                before = target.getMaxHp();
                after = Math.max(1, before + delta);
                target.setMaxHp(after);
                if (target.getHp() > after) {
                    target.setHp(after);
                }
            }
            default -> {
                return;
            }
        }
        int applied = Math.abs(after - before);
        logs.add("  └ " + target.getName() + " " + attrLabel + " " + dirLabel + " " + applied
                + (segments > 1 ? "（第" + (segIdx + 1) + "段）" : "")
                + "（" + before + "→" + after + "）");
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
