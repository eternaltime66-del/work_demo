package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleUnitSnapVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行动值战斗演算：
 * <ul>
 *   <li>每 tick 全局已经过行动值 +1，各存活单位行动进度 +1</li>
 *   <li>进度达到自身行动阈值则行动：释放可用技能，结算效果</li>
 *   <li>敌方全灭胜利 / 我方全灭失败</li>
 * </ul>
 */
public class BattleEngine {

    public static final int MAX_TICKS = 5000;

    private final List<BattleRuntimeUnit> units = new ArrayList<>();
    private final Map<String, List<SkillCharge>> chargesBySkill = new HashMap<>();
    private final Map<String, List<SkillEffect>> effectsBySkill = new HashMap<>();
    private final BattleStatBoard board = new BattleStatBoard();
    private final List<String> logs = new ArrayList<>();

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
            // 同 tick 多单位行动：先我方后敌方，同侧按位置
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

    private void accrueCharges(int elapsedBefore, int delta) {
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
                    }
                }
            }
        }
    }

    private void act(BattleRuntimeUnit actor) {
        ActiveSkill skill = pickSkill(actor);
        if (skill == null) {
            logActHead(actor.getName() + " 行动但无可释放技能");
            return;
        }
        castSkill(actor, skill);
        // 本次行动内：普攻等刚把充能技能攒满时，立刻连放，不等下次行动条
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
        // 顺序：释放 → 造成伤害/效果 → 充能累计 →（外层再连放已满充能技能）
        logActHead(actor.getName() + " 释放「" + skill.getName() + "」");

        List<SkillEffect> effects = effectsBySkill.getOrDefault(skill.getId(), List.of());
        for (SkillEffect effect : effects) {
            applyEffect(actor, skill, effect);
        }

        accrueSkillCharges(actor, SkillChargeEvent.CAST, skill);
    }

    /** 行动组头日志：首行「行动值 N」，次行动作正文（前端按换行展示） */
    private void logActHead(String body) {
        logs.add("行动值 " + board.getElapsedActionValue() + "\n" + body);
    }

    /** 对单位身上所有技能的 SKILL_CHARGE 条件结算一次事件充能 */
    private void accrueSkillCharges(BattleRuntimeUnit owner, SkillChargeEvent event, ActiveSkill trigger) {
        if (owner == null || !owner.alive() || trigger == null) {
            return;
        }
        for (ActiveSkill skill : owner.getSkills()) {
            if (skill == null || skill.getId() == null) {
                continue;
            }
            // 普攻自身不吃技能充能条件
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

    /** 已就绪的充能技能（非普攻）：用于行动内连放 */
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
            // 普攻推荐：所需=自身行动值，配合「每1行动值+1充能」即可每回合出手
            return Math.max(0, actor.getAction());
        }
        return skill.getNeedCharge() == null ? 0 : Math.max(0, skill.getNeedCharge());
    }

    private void applyEffect(BattleRuntimeUnit caster, ActiveSkill skill, SkillEffect effect) {
        if (effect == null || effect.getEffectType() == null) {
            return;
        }
        List<BattleRuntimeUnit> targets = SkillTargetResolver.resolve(effect.getTargetType(), caster, units);
        if (targets.isEmpty()) {
            logs.add("  └ 无目标（" + effect.getName() + "）");
            return;
        }
        int segments = effect.getHitSegments() == null || effect.getHitSegments() < 1 ? 1 : effect.getHitSegments();
        for (BattleRuntimeUnit target : targets) {
            if (!target.alive()) {
                continue;
            }
            for (int i = 0; i < segments; i++) {
                double raw = FormulaEvalUnit.eval(effect.getFormulaJson(), caster, target);
                int amount = (int) Math.max(0, Math.round(raw));
                if (effect.getEffectType() == SkillEffectType.DAMAGE) {
                    int dealt = Math.max(1, amount - Math.max(0, target.getDef() / 10));
                    target.setHp(Math.max(0, target.getHp() - dealt));
                    board.recordReceive(target.getUnitId(), skill.getId(), skill.getSkillType());
                    // 受到伤害时：按目标身上技能的「受到」充能条件结算（每段结算一次）
                    accrueSkillCharges(target, SkillChargeEvent.RECEIVE, skill);
                    logs.add("  └ 对 " + target.getName() + " 造成 " + dealt + " 伤害"
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : "")
                            + (target.alive() ? "" : "，击杀"));
                } else if (effect.getEffectType() == SkillEffectType.HEAL) {
                    int heal = amount;
                    target.setHp(Math.min(target.getMaxHp(), target.getHp() + heal));
                    logs.add("  └ 治疗 " + target.getName() + " " + heal
                            + (segments > 1 ? "（第" + (i + 1) + "段）" : ""));
                } else if (effect.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                    applyAttrModify(target, effect, amount, segments, i);
                }
            }
        }
    }

    /**
     * 属性修改：公式算出变更量，按增加/减少应用到目标。
     * 攻击/防御最低 0；最大生命最低 1，并钳制当前生命不超过上限。
     */
    private void applyAttrModify(BattleRuntimeUnit target, SkillEffect effect, int amount, int segments, int segIdx) {
        if (effect.getAttrKey() == null || effect.getAttrDir() == null) {
            logs.add("  └ 属性修改未配置（" + (effect.getName() != null ? effect.getName() : "") + "）");
            return;
        }
        boolean decrease = effect.getAttrDir() == AttrModifyDirection.DECREASE;
        int delta = decrease ? -amount : amount;
        String attrLabel = effect.getAttrKey().getLabel();
        String dirLabel = effect.getAttrDir().getLabel();
        int before;
        int after;
        switch (effect.getAttrKey()) {
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
}
