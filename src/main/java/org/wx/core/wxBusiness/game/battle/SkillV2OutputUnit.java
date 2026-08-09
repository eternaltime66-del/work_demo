package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.BuffStackMode;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.DamageSourceKind;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;
import org.wx.core.wxBusiness.game.entity.vo.BattleEventVo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 技能 V2：统一输出与 BUFF 结算（由 BattleEngine 调用）。
 */
public final class SkillV2OutputUnit {

    public interface Host {
        List<BattleRuntimeUnit> units();

        BattleStatBoard board();

        List<String> logs();

        BattleEventVo emit(String type);

        void notifyStatChanged();

        BuffDef buffDefOf(String buffDefId);

        void onDamage(
                BattleRuntimeUnit dealer,
                BattleRuntimeUnit target,
                int dealt,
                DamageSourceKind kind,
                DamageElement element
        );
    }

    private SkillV2OutputUnit() {
    }

    public static void applyOutputs(
            Host host,
            BattleRuntimeUnit owner,
            List<SkillOutput> outputs,
            AnchorEvalContext ctx,
            String logPrefix,
            boolean revokeableAttr
    ) {
        applyOutputs(host, owner, outputs, ctx, logPrefix, revokeableAttr, DamageSourceKind.PASSIVE);
    }

    public static void applyOutputs(
            Host host,
            BattleRuntimeUnit owner,
            List<SkillOutput> outputs,
            AnchorEvalContext ctx,
            String logPrefix,
            boolean revokeableAttr,
            DamageSourceKind damageKind
    ) {
        if (host == null || owner == null || outputs == null || outputs.isEmpty()) {
            return;
        }
        String prefix = logPrefix != null ? logPrefix : "  └ ";
        DamageSourceKind kind = damageKind != null ? damageKind : DamageSourceKind.PASSIVE;
        for (SkillOutput out : outputs) {
            if (out == null || out.getOutputKind() == null) {
                continue;
            }
            if (!rollRate(out.getTriggerRate())) {
                host.logs().add(prefix + "「" + label(out) + "」未触发");
                continue;
            }
            switch (out.getOutputKind()) {
                case ATTR -> applyAttrOutput(host, owner, out, ctx, prefix, revokeableAttr);
                case EFFECT -> applyEffectOutput(host, owner, out, ctx, prefix, kind);
                case APPEND_BUFF -> applyAppendBuff(host, owner, out, ctx, prefix);
            }
        }
    }

    private static void applyAttrOutput(
            Host host, BattleRuntimeUnit owner, SkillOutput out, AnchorEvalContext ctx,
            String prefix, boolean revokeable
    ) {
        List<BattleRuntimeUnit> targets = resolveTargets(out.getTargetType(), owner, host.units(), ctx);
        int segments = Math.max(1, out.getHitSegments() == null ? 1 : out.getHitSegments());
        int dur = out.getDurationAv() == null ? 0 : out.getDurationAv();
        for (BattleRuntimeUnit target : targets) {
            if (target == null || !target.alive()) {
                continue;
            }
            for (int i = 0; i < segments; i++) {
                double raw = FormulaEvalUnit.eval(out.getFormulaJson(), owner, target, host.board(), ctx);
                int amount = (int) Math.max(0, Math.round(raw));
                String sourceKey = "SOUT#" + (out.getId() != null ? out.getId() : label(out))
                        + "#" + target.getUnitId();
                applyFlatOrRatio(host, target, out.getAttrKey(), out.getAttrDir(), amount,
                        revokeable || dur > 0, sourceKey, dur, prefix);
            }
        }
    }

    private static void applyEffectOutput(
            Host host, BattleRuntimeUnit owner, SkillOutput out, AnchorEvalContext ctx, String prefix,
            DamageSourceKind damageKind
    ) {
        List<BattleRuntimeUnit> targets = resolveTargets(out.getTargetType(), owner, host.units(), ctx);
        int segments = Math.max(1, out.getHitSegments() == null ? 1 : out.getHitSegments());
        SkillEffectType et = out.getEffectType() == null ? SkillEffectType.DAMAGE : out.getEffectType();
        DamageElement el = out.getDamageElement() == null ? DamageElement.PHYSICAL : out.getDamageElement();
        DamageSourceKind kind = damageKind != null ? damageKind : DamageSourceKind.PASSIVE;
        for (BattleRuntimeUnit target : targets) {
            if (target == null || !target.alive()) {
                continue;
            }
            for (int i = 0; i < segments; i++) {
                double raw = FormulaEvalUnit.eval(out.getFormulaJson(), owner, target, host.board(), ctx);
                int amount = (int) Math.max(0, Math.round(raw));
                if (et == SkillEffectType.HEAL) {
                    int before = target.getHp();
                    target.setHp(Math.min(target.getMaxHp(), before + amount));
                    int got = target.getHp() - before;
                    host.board().recordHeal(owner.getUnitId(), target.getUnitId(), got);
                    host.logs().add(prefix + "治疗 " + target.getName() + " " + got);
                    BattleEventVo ev = host.emit("HEAL");
                    ev.setUid(owner.getUnitId());
                    ev.setTarget(target.getUnitId());
                    ev.setValue(got);
                    ev.setHpAfter(target.getHp());
                    ev.setMaxHp(target.getMaxHp());
                    host.notifyStatChanged();
                } else {
                    int rawDmg = DamageRatioUnit.afterDefense(amount, owner, target);
                    int dealt = DamageRatioUnit.finalizeDealt(rawDmg, owner, target, el);
                    host.onDamage(owner, target, dealt, kind, el);
                }
            }
        }
    }

    private static void applyAppendBuff(
            Host host, BattleRuntimeUnit owner, SkillOutput out, AnchorEvalContext ctx, String prefix
    ) {
        BuffDef def = host.buffDefOf(out.getBuffDefId());
        if (def == null) {
            host.logs().add(prefix + "BUFF 不存在：" + out.getBuffDefId());
            return;
        }
        List<BattleRuntimeUnit> targets = resolveTargets(
                out.getTargetType() != null ? out.getTargetType() : SkillEffectTarget.SELF,
                owner, host.units(), ctx);
        for (BattleRuntimeUnit target : targets) {
            if (target == null || !target.alive()) {
                continue;
            }
            mountBuff(host, owner, target, def, prefix);
        }
    }

    public static void mountBuff(Host host, BattleRuntimeUnit caster, BattleRuntimeUnit target, BuffDef def, String prefix) {
        if (def == null || target == null) {
            return;
        }
        // 同轴：先清到期，再判定叠加/施加，避免「不可叠加」后立刻到期断周期
        expireDueBuffs(host, target);
        BuffStackMode stackMode = def.getStackMode() == null ? BuffStackMode.NONE : def.getStackMode();
        BuffInstance existing = findBuff(target, def.getId());
        if (existing != null) {
            if (stackMode == BuffStackMode.NONE) {
                // 闪避：不可叠层时仍刷新持续（同一次施加刷新时长）
                if (def.getBuffKind() == BuffKind.DODGE) {
                    refreshExpire(host, existing, def);
                    host.logs().add(prefix + target.getName() + " 刷新 BUFF「" + def.getName() + "」"
                            + dodgeLogSuffix(def));
                    emitBuff(host, target, def, existing);
                    return;
                }
                host.logs().add(prefix + target.getName() + " 已有 BUFF「" + def.getName() + "」，不可叠加");
                return;
            }
            int max = def.getMaxStacks() == null || def.getMaxStacks() <= 0 ? Integer.MAX_VALUE : def.getMaxStacks();
            if (existing.getStacks() < max) {
                existing.setStacks(existing.getStacks() + 1);
                applyBuffStackDelta(host, target, def, existing, 1);
            }
            refreshExpire(host, existing, def);
            host.logs().add(prefix + target.getName() + " BUFF「" + def.getName() + "」叠层=" + existing.getStacks());
            emitBuff(host, target, def, existing);
            return;
        }
        BuffInstance inst = new BuffInstance();
        inst.setInstanceId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        inst.setBuffDefId(def.getId());
        inst.setBuffCode(def.getCode());
        inst.setName(def.getName());
        inst.setBuffKind(def.getBuffKind());
        inst.setStacks(1);
        inst.setBeneficial(!Boolean.FALSE.equals(def.getBeneficial()));
        inst.setDispelable(!Boolean.FALSE.equals(def.getDispelable()));
        inst.setCasterUnitId(caster != null ? caster.getUnitId() : null);
        inst.setDamageElement(def.getDamageElement() == null ? DamageElement.PHYSICAL : def.getDamageElement());
        inst.setAttrKey(def.getAttrKey());
        refreshExpire(host, inst, def);
        if (def.getBuffKind() == BuffKind.PULSE) {
            int every = def.getPulseEveryAv() == null || def.getPulseEveryAv() < 1 ? 100 : def.getPulseEveryAv();
            inst.setPulseEveryAv(every);
            inst.setNextPulseAt(host.board().getElapsedActionValue() + every);
            inst.setLockedElementBonus(ElementDamageUnit.elementBonus(caster, inst.getDamageElement())
                    + (caster == null ? 0 : caster.getElementDmgBonus()));
        }
        target.getBuffs().add(inst);
        if (def.getBuffKind() == BuffKind.ATTR) {
            applyBuffStackDelta(host, target, def, inst, 1);
            inst.setJudgeEffectActive(true);
        }
        host.logs().add(prefix + target.getName() + " 获得 BUFF「" + def.getName() + "」"
                + (def.getBuffKind() == BuffKind.DODGE ? dodgeLogSuffix(def) : ""));
        emitBuff(host, target, def, inst);
        host.notifyStatChanged();
    }

    private static String dodgeLogSuffix(BuffDef def) {
        if (def == null) {
            return "";
        }
        int chance = def.getDodgeChance() == null ? 0 : Math.min(100, Math.max(0, def.getDodgeChance()));
        String scope = PassiveSkillMatchUnit.matchScopeLabel(
                def.getSkillMatchMode(),
                def.getMatchSkillType(),
                def.getMatchSkillSchool(),
                def.getMatchDamageElement(),
                def.getMatchSkillId()
        );
        return "（闪避" + chance + "% · " + scope + "）";
    }

    /** 全场到期结算（推进时间轴 / 施法前调用） */
    public static void expireDueBuffs(Host host) {
        if (host == null) {
            return;
        }
        for (BattleRuntimeUnit u : host.units()) {
            expireDueBuffs(host, u);
        }
    }

    /** 指定单位到期结算 */
    public static void expireDueBuffs(Host host, BattleRuntimeUnit u) {
        if (host == null || u == null) {
            return;
        }
        int now = host.board().getElapsedActionValue();
        List<BuffInstance> copy = new ArrayList<>(u.getBuffs());
        for (BuffInstance b : copy) {
            if (b == null) {
                continue;
            }
            if (b.getExpireAtElapsed() != null && now >= b.getExpireAtElapsed()) {
                removeBuff(host, u, b, "到期");
            }
        }
    }

    public static void tickBuffs(Host host) {
        if (host == null) {
            return;
        }
        // 同轴优先：先到期，再脉冲
        expireDueBuffs(host);
        int now = host.board().getElapsedActionValue();
        for (BattleRuntimeUnit u : host.units()) {
            if (u == null) {
                continue;
            }
            List<BuffInstance> copy = new ArrayList<>(u.getBuffs());
            for (BuffInstance b : copy) {
                if (b == null) {
                    continue;
                }
                if (b.getBuffKind() == BuffKind.PULSE
                        && b.getNextPulseAt() != null
                        && now >= b.getNextPulseAt()) {
                    pulseOnce(host, u, b);
                    b.setNextPulseAt(now + Math.max(1, b.getPulseEveryAv()));
                }
            }
        }
    }

    public static void scanJudgeBuffs(Host host) {
        if (host == null) {
            return;
        }
        for (BattleRuntimeUnit u : host.units()) {
            if (u == null || !u.alive()) {
                continue;
            }
            for (BuffInstance b : new ArrayList<>(u.getBuffs())) {
                if (b == null) {
                    continue;
                }
                BuffDef def = host.buffDefOf(b.getBuffDefId());
                if (def == null) {
                    continue;
                }
                if (def.getBuffKind() == BuffKind.JUDGE_ATTR) {
                    boolean ok = evalJudge(host, u, def);
                    if (ok && !b.isJudgeEffectActive()) {
                        applyBuffStackDelta(host, u, def, b, b.getStacks());
                        b.setJudgeEffectActive(true);
                    } else if (!ok && b.isJudgeEffectActive()) {
                        applyBuffStackDelta(host, u, def, b, -b.getStacks());
                        b.setJudgeEffectActive(false);
                    }
                } else if (def.getBuffKind() == BuffKind.JUDGE_BURST) {
                    if (evalJudge(host, u, def)) {
                        burstOnce(host, u, def, b);
                        removeBuff(host, u, b, "爆发触发");
                    }
                }
            }
        }
    }

    private static void pulseOnce(Host host, BattleRuntimeUnit holder, BuffInstance b) {
        BuffDef def = host.buffDefOf(b.getBuffDefId());
        if (def == null) {
            return;
        }
        BattleRuntimeUnit caster = findUnit(host, b.getCasterUnitId());
        if (caster == null) {
            caster = holder;
        }
        SkillEffectType et = def.getPulseEffectType() == null ? SkillEffectType.DAMAGE : def.getPulseEffectType();
        SkillEffectTarget tt = def.getPulseTargetType() == null ? SkillEffectTarget.SELF : def.getPulseTargetType();
        AnchorEvalContext ctx = new AnchorEvalContext();
        ctx.setAllUnits(host.units());
        ctx.setPulseCaster(caster);
        // 以持有者为「自己」解析目标；SELF 即持有者本人
        List<BattleRuntimeUnit> targets = SkillTargetResolver.resolveWithAnchor(tt, holder, host.units(), ctx);
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (BattleRuntimeUnit target : targets) {
            if (target == null || !target.alive()) {
                continue;
            }
            double raw = FormulaEvalUnit.eval(def.getFormulaJson(), caster, target, host.board(), ctx) * b.getStacks();
            int amount = (int) Math.max(0, Math.round(raw));
            if (et == SkillEffectType.HEAL) {
                int before = target.getHp();
                target.setHp(Math.min(target.getMaxHp(), before + amount));
                host.board().recordHeal(caster.getUnitId(), target.getUnitId(), target.getHp() - before);
                host.logs().add("  └ 脉冲「" + b.getName() + "」治疗 " + target.getName() + " " + (target.getHp() - before));
            } else {
                int base = DamageRatioUnit.afterDefense(amount, caster, target);
                base = Math.max(1, (int) Math.round(base * (1D + b.getLockedElementBonus() / 100D)));
                int dealt = DamageRatioUnit.finalizeDealt(base, caster, target, b.getDamageElement());
                host.onDamage(caster, target, dealt, DamageSourceKind.PULSE_BUFF, b.getDamageElement());
            }
        }
    }

    private static void burstOnce(Host host, BattleRuntimeUnit target, BuffDef def, BuffInstance b) {
        BattleRuntimeUnit caster = findUnit(host, b.getCasterUnitId());
        if (caster == null) {
            caster = target;
        }
        AnchorEvalContext ctx = new AnchorEvalContext();
        ctx.setAllUnits(host.units());
        double raw = FormulaEvalUnit.eval(def.getFormulaJson(), caster, target, host.board(), ctx) * b.getStacks();
        int amount = (int) Math.max(0, Math.round(raw));
        SkillEffectType et = def.getPulseEffectType() == null ? SkillEffectType.DAMAGE : def.getPulseEffectType();
        if (et == SkillEffectType.HEAL) {
            target.setHp(Math.min(target.getMaxHp(), target.getHp() + amount));
        } else if (et == SkillEffectType.ATTR_MODIFY) {
            applyFlatOrRatio(host, target, def.getAttrKey(), def.getAttrDir(), amount, false, null, 0, "  └ ");
        } else {
            int dealt = DamageRatioUnit.finalizeDealt(
                    DamageRatioUnit.afterDefense(amount, caster, target), caster, target, def.getDamageElement());
            host.onDamage(caster, target, dealt, DamageSourceKind.PASSIVE, def.getDamageElement());
        }
        host.logs().add("  └ 爆发 BUFF「" + def.getName() + "」对 " + target.getName() + " 生效");
    }

    private static boolean evalJudge(Host host, BattleRuntimeUnit owner, BuffDef def) {
        AnchorEvalContext ctx = new AnchorEvalContext();
        ctx.setAllUnits(host.units());
        double left = FormulaEvalUnit.eval(def.getLeftFormulaJson(), owner, owner, host.board(), ctx);
        double right = FormulaEvalUnit.eval(def.getRightFormulaJson(), owner, owner, host.board(), ctx);
        CompareOp op = def.getCompareOp() == null ? CompareOp.LTE : def.getCompareOp();
        return compareSafe(left, op, right);
    }

    public static void removeBuff(Host host, BattleRuntimeUnit target, BuffInstance b, String reason) {
        if (target == null || b == null) {
            return;
        }
        BuffDef def = host.buffDefOf(b.getBuffDefId());
        if (def != null && (b.getBuffKind() == BuffKind.ATTR || b.isJudgeEffectActive())) {
            applyBuffStackDelta(host, target, def, b, -b.getStacks());
        }
        target.getBuffs().remove(b);
        host.logs().add("  └ " + target.getName() + " BUFF「" + b.getName() + "」结束（" + reason + "）");
        BattleEventVo ev = host.emit("BUFF_END");
        ev.setUid(target.getUnitId());
        ev.setTarget(target.getUnitId());
        ev.setAttrKey(b.getBuffDefId());
    }

    private static void applyBuffStackDelta(Host host, BattleRuntimeUnit target, BuffDef def, BuffInstance inst, int stackDelta) {
        if (stackDelta == 0 || def.getAttrKey() == null || inst == null) {
            return;
        }
        double raw = FormulaEvalUnit.eval(def.getFormulaJson(), target, target, host.board(), null);
        int amount = (int) Math.max(0, Math.round(raw));
        AttrModifyKey key = def.getAttrKey();
        if (key.getModKind() == AttrModifyKey.ModKind.MULT_PERCENT && DamageRatioUnit.isDamageRatioKey(key)) {
            int once = def.getAttrDir() == AttrModifyDirection.DECREASE ? -amount : amount;
            double factor = Math.pow(DamageRatioUnit.factorFromSignedPercent(once), Math.abs(stackDelta));
            if (stackDelta > 0) {
                DamageRatioUnit.applyMultFactor(target, key, factor);
            } else {
                DamageRatioUnit.revokeMultFactor(target, key, factor);
            }
            return;
        }
        // FINAL_*：百分比只用于计算生效绝对值；撤回时扣生效值，禁止对当前面板再反算百分比
        if (isFinalPanelPercent(key)) {
            applyOrRevokeFinalPercent(target, def, inst, amount, stackDelta);
            return;
        }
        int signed = (def.getAttrDir() == AttrModifyDirection.DECREASE ? -amount : amount) * stackDelta;
        mutateAttr(target, key, signed, inst, stackDelta > 0);
    }

    private static boolean isFinalPanelPercent(AttrModifyKey key) {
        return key == AttrModifyKey.FINAL_ATK
                || key == AttrModifyKey.FINAL_HP
                || key == AttrModifyKey.FINAL_DEF;
    }

    private static void applyOrRevokeFinalPercent(
            BattleRuntimeUnit target, BuffDef def, BuffInstance inst, int amount, int stackDelta
    ) {
        AttrModifyKey key = def.getAttrKey();
        int pct = def.getAttrDir() == AttrModifyDirection.DECREASE ? -amount : amount;
        if (stackDelta > 0) {
            for (int i = 0; i < stackDelta; i++) {
                int base = readFinalPanelBase(target, key);
                int delta = (int) Math.round(base * (pct / 100D));
                writeFinalPanelDelta(target, key, delta);
                inst.setAppliedFlatTotal(inst.getAppliedFlatTotal() + delta);
                inst.setAppliedFlatPerStack(delta);
                inst.setRatioAddPerStack(pct);
            }
            return;
        }
        int removeStacks = Math.abs(stackDelta);
        int curStacks = Math.max(1, inst.getStacks());
        int revoke;
        if (removeStacks >= curStacks) {
            revoke = inst.getAppliedFlatTotal();
            inst.setAppliedFlatTotal(0);
        } else {
            revoke = (int) Math.round(inst.getAppliedFlatTotal() * (removeStacks / (double) curStacks));
            inst.setAppliedFlatTotal(inst.getAppliedFlatTotal() - revoke);
        }
        writeFinalPanelDelta(target, key, -revoke);
    }

    private static int readFinalPanelBase(BattleRuntimeUnit target, AttrModifyKey key) {
        return switch (key) {
            case FINAL_ATK -> target.getAtk();
            case FINAL_HP -> target.getMaxHp();
            case FINAL_DEF -> target.getDef();
            default -> 0;
        };
    }

    private static void writeFinalPanelDelta(BattleRuntimeUnit target, AttrModifyKey key, int delta) {
        if (delta == 0) {
            return;
        }
        switch (key) {
            case FINAL_ATK -> target.setAtk(Math.max(0, target.getAtk() + delta));
            case FINAL_HP -> {
                int after = Math.max(1, target.getMaxHp() + delta);
                if (delta > 0) {
                    target.setHp(target.getHp() + delta);
                }
                target.setMaxHp(after);
                if (target.getHp() > after) {
                    target.setHp(after);
                }
            }
            case FINAL_DEF -> target.setDef(Math.max(0, target.getDef() + delta));
            default -> {
            }
        }
    }

    private static void applyFlatOrRatio(
            Host host, BattleRuntimeUnit target, AttrModifyKey key, AttrModifyDirection dir,
            int amount, boolean timed, String sourceKey, int durationAv, String prefix
    ) {
        if (key == null || dir == null) {
            return;
        }
        if (timed && sourceKey != null) {
            TimedAttrBuff exist = target.findBuff(sourceKey);
            if (exist != null) {
                if (durationAv > 0) {
                    exist.setExpireAtElapsed(host.board().getElapsedActionValue() + durationAv);
                }
                return;
            }
        }
        int signed = dir == AttrModifyDirection.DECREASE ? -Math.abs(amount) : Math.abs(amount);
        TimedAttrBuff snap = new TimedAttrBuff();
        snap.setSourceKey(sourceKey);
        snap.setAttrKey(key);
        mutateAttr(target, key, signed, snap, true);
        if (timed && sourceKey != null) {
            snap.setExpireAtElapsed(durationAv > 0 ? host.board().getElapsedActionValue() + durationAv : null);
            target.getTimedBuffs().add(snap);
        }
        host.logs().add(prefix + target.getName() + " " + key.getLabel()
                + (dir == AttrModifyDirection.DECREASE ? "减少" : "增加") + Math.abs(amount));
        BattleEventVo ev = host.emit("BUFF");
        ev.setUid(target.getUnitId());
        ev.setTarget(target.getUnitId());
        ev.setAttrKey(key.name());
        ev.setDuration(durationAv);
        ev.setValue(Math.abs(amount));
        host.notifyStatChanged();
    }

    private static void mutateAttr(BattleRuntimeUnit target, AttrModifyKey key, int signed, Object track, boolean applying) {
        if (key == null) {
            return;
        }
        switch (key.getModKind()) {
            case FLAT -> {
                switch (key) {
                    case ATK -> target.setAtk(Math.max(0, target.getAtk() + signed));
                    case DEF -> target.setDef(Math.max(0, target.getDef() + signed));
                    case MAX_HP -> {
                        target.setMaxHp(Math.max(1, target.getMaxHp() + signed));
                        if (target.getHp() > target.getMaxHp()) {
                            target.setHp(target.getMaxHp());
                        }
                    }
                    default -> {
                    }
                }
                if (track instanceof TimedAttrBuff tb && applying) {
                    tb.setAppliedFlat(signed);
                }
                if (track instanceof BuffInstance bi && applying) {
                    bi.setAppliedFlatPerStack(Math.abs(signed));
                }
            }
            case ADD_PERCENT -> {
                switch (key) {
                    case LIFE_STEAL -> target.setLifeStealAdd(target.getLifeStealAdd() + signed);
                    case ATK_SPEED -> target.setAtkSpeedAdd(target.getAtkSpeedAdd() + signed);
                    case FINAL_ATK, FINAL_HP, FINAL_DEF -> {
                        // 应走 applyOrRevokeFinalPercent；此处兜底：施加记绝对值，撤回用已记值
                        if (applying) {
                            int base = readFinalPanelBase(target, key);
                            int delta = (int) Math.round(base * (signed / 100D));
                            writeFinalPanelDelta(target, key, delta);
                            if (track instanceof TimedAttrBuff tb) {
                                tb.setAppliedFlat(delta);
                                tb.setRatioAdd(signed);
                            }
                            if (track instanceof BuffInstance bi) {
                                bi.setAppliedFlatTotal(bi.getAppliedFlatTotal() + delta);
                                bi.setAppliedFlatPerStack(delta);
                                bi.setRatioAddPerStack(signed);
                            }
                        } else if (track instanceof TimedAttrBuff tb) {
                            writeFinalPanelDelta(target, key, -tb.getAppliedFlat());
                        } else if (track instanceof BuffInstance bi) {
                            writeFinalPanelDelta(target, key, -bi.getAppliedFlatTotal());
                            bi.setAppliedFlatTotal(0);
                        }
                    }
                    default -> {
                    }
                }
                if (track instanceof TimedAttrBuff tb && applying
                        && key != AttrModifyKey.FINAL_ATK
                        && key != AttrModifyKey.FINAL_HP
                        && key != AttrModifyKey.FINAL_DEF) {
                    tb.setRatioAdd(signed);
                }
            }
            case MULT_PERCENT -> {
                if (!DamageRatioUnit.isDamageRatioKey(key)) {
                    return;
                }
                double factor = DamageRatioUnit.factorFromSignedPercent(signed);
                if (applying) {
                    DamageRatioUnit.applyMultFactor(target, key, factor);
                    if (track instanceof TimedAttrBuff tb) {
                        tb.setRatioFactor(factor);
                        tb.setRatioAdd(signed);
                    }
                } else {
                    // signed 已取反：用原增幅因子做除法撤销
                    DamageRatioUnit.revokeMultFactor(target, key, DamageRatioUnit.factorFromSignedPercent(-signed));
                }
            }
            default -> {
            }
        }
    }

    private static List<BattleRuntimeUnit> resolveTargets(
            SkillEffectTarget type, BattleRuntimeUnit owner, List<BattleRuntimeUnit> all, AnchorEvalContext ctx
    ) {
        return SkillTargetResolver.resolveWithAnchor(
                type == null ? SkillEffectTarget.SELF : type, owner, all, ctx);
    }

    private static BuffInstance findBuff(BattleRuntimeUnit u, String buffDefId) {
        if (u == null || buffDefId == null) {
            return null;
        }
        for (BuffInstance b : u.getBuffs()) {
            if (b != null && buffDefId.equals(b.getBuffDefId())) {
                return b;
            }
        }
        return null;
    }

    private static void refreshExpire(Host host, BuffInstance inst, BuffDef def) {
        int dur = def.getDurationAv() == null ? 0 : def.getDurationAv();
        inst.setExpireAtElapsed(dur > 0 ? host.board().getElapsedActionValue() + dur : null);
    }

    private static void emitBuff(Host host, BattleRuntimeUnit target, BuffDef def, BuffInstance inst) {
        BattleEventVo ev = host.emit("BUFF");
        ev.setUid(target.getUnitId());
        ev.setTarget(target.getUnitId());
        ev.setAttrKey(def.getId());
        ev.setStack(inst.getStacks());
        ev.setDuration(def.getDurationAv() == null ? 0 : def.getDurationAv());
    }

    private static BattleRuntimeUnit findUnit(Host host, String unitId) {
        if (unitId == null) {
            return null;
        }
        for (BattleRuntimeUnit u : host.units()) {
            if (u != null && unitId.equals(u.getUnitId())) {
                return u;
            }
        }
        return null;
    }

    private static boolean rollRate(Integer rate) {
        int r = rate == null ? 100 : rate;
        if (r >= 100) {
            return true;
        }
        if (r <= 0) {
            return false;
        }
        return ThreadLocalRandom.current().nextInt(100) < r;
    }

    private static String label(SkillOutput out) {
        return SkillOutputDescUnit.display(out);
    }

    public static boolean compareSafe(double left, CompareOp op, double right) {
        if (op == null) {
            return false;
        }
        return switch (op) {
            case GT -> left > right;
            case GTE -> left >= right;
            case LT -> left < right;
            case LTE -> left <= right;
            case EQ -> Math.abs(left - right) < 1e-9;
        };
    }
}

