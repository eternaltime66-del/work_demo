package org.wx.core.wxBusiness.game.battle;

import org.junit.jupiter.api.Test;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.DamageSourceKind;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.BuffStackMode;
import org.wx.core.wxBusiness.game.entity.enums.CombatEventType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeScope;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;
import org.wx.core.wxBusiness.game.entity.vo.BattleEventVo;
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;

import static org.assertj.core.api.Assertions.assertThat;

class BattleEngineSafetyTests {

    private static final String TEN = "[{\"kind\":\"PARAM\",\"paramMode\":\"LITERAL\",\"value\":\"10\"}]";

    @Test
    void deadTargetCannotBeDamagedOrKilledTwice() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit dealer = unit("dealer", BattleSide.ALLY, 100);
        BattleRuntimeUnit target = unit("target", BattleSide.ENEMY, 10);
        engine.addUnit(dealer);
        engine.addUnit(target);

        engine.onDamage(dealer, target, 100, DamageSourceKind.ACTIVE_SKILL, DamageElement.PHYSICAL);
        engine.onDamage(dealer, target, 100, DamageSourceKind.ACTIVE_SKILL, DamageElement.PHYSICAL);

        assertThat(target.getHp()).isZero();
        assertThat(engine.board().getDealDamageAmount("dealer")).isEqualTo(10);
        assertThat(engine.logs().stream().filter(line -> line.contains("击杀"))).hasSize(1);
    }

    @Test
    void attackSpeedChangeRecalculatesNormalChargeThresholdWithoutChangingCharge() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit unit = unit("hero", BattleSide.ALLY, 100);
        unit.setBaseAction(100);
        unit.setAction(100);
        unit.addCharge("normal", 50);

        unit.setAtkSpeedAdd(100);
        engine.onAttackSpeedChanged(unit);
        assertThat(unit.getAction()).isEqualTo(50);
        assertThat(unit.getCharge("normal")).isEqualTo(50);

        unit.setAtkSpeedAdd(0);
        engine.onAttackSpeedChanged(unit);
        assertThat(unit.getAction()).isEqualTo(100);
        assertThat(unit.getCharge("normal")).isEqualTo(50);
    }

    @Test
    void castEventUsesTheSameRandomTargetThatWasActuallyHit() {
        for (int round = 0; round < 20; round++) {
            BattleEngine engine = new BattleEngine();
            BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
            BattleRuntimeUnit enemyA = unit("enemy-a", BattleSide.ENEMY, 100);
            BattleRuntimeUnit enemyB = unit("enemy-b", BattleSide.ENEMY, 100);

            ActiveSkill normal = new ActiveSkill();
            normal.setId("normal");
            normal.setName("normal");
            normal.setSkillType(ActiveSkillType.NORMAL);
            normal.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
            normal.setNeedCharge(0);
            actor.getSkills().add(normal);

            SkillOutput output = new SkillOutput();
            output.setId("random-hit");
            output.setSkillId(normal.getId());
            output.setOutputKind(SkillOutputKind.EFFECT);
            output.setEffectType(SkillEffectType.DAMAGE);
            output.setTargetType(SkillEffectTarget.RANDOM_ENEMY);
            output.setFormulaJson(TEN);
            output.setHitSegments(1);
            output.setTriggerRate(100);

            engine.addUnit(actor);
            engine.addUnit(enemyA);
            engine.addUnit(enemyB);
            putActionCharge(engine, normal, 1, 1);
            engine.putSkillOutputs(normal.getId(), java.util.List.of(output));

            BattleResultVo result = engine.run();
            BattleEventVo cast = result.getEvents().stream()
                    .filter(event -> "CAST".equals(event.getType()))
                    .findFirst().orElseThrow();
            BattleEventVo hit = result.getEvents().stream()
                    .filter(event -> "HIT".equals(event.getType()))
                    .findFirst().orElseThrow();

            assertThat(cast.getTargets()).containsExactly(hit.getTarget());
        }
    }

    @Test
    void pulseExecutesOnItsExpiryAxisBeforeBuffIsRemoved() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit holder = unit("holder", BattleSide.ALLY, 100);
        holder.setHp(50);
        engine.addUnit(holder);

        BuffDef def = new BuffDef();
        def.setId("hot");
        def.setName("hot");
        def.setBuffKind(BuffKind.PULSE);
        def.setStackMode(BuffStackMode.NONE);
        def.setDurationAv(100);
        def.setPulseEveryAv(100);
        def.setPulseEffectType(SkillEffectType.HEAL);
        def.setPulseTargetType(SkillEffectTarget.SELF);
        def.setFormulaJson(TEN);
        engine.putBuffDef(def);

        SkillV2OutputUnit.mountBuff(engine, holder, holder, def, "");
        engine.board().setElapsedActionValue(100);
        SkillV2OutputUnit.tickBuffs(engine);

        assertThat(holder.getHp()).isEqualTo(60);
        assertThat(holder.getBuffs()).isEmpty();
    }

    @Test
    void combatPassiveHasIndependentSourceAndDoesNotPolluteCastTargets() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        BattleRuntimeUnit enemyA = unit("enemy-a", BattleSide.ENEMY, 100);
        BattleRuntimeUnit enemyB = unit("enemy-b", BattleSide.ENEMY, 100);

        ActiveSkill normal = new ActiveSkill();
        normal.setId("normal");
        normal.setName("normal");
        normal.setSkillType(ActiveSkillType.NORMAL);
        normal.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
        normal.setNeedCharge(0);
        actor.getSkills().add(normal);

        SkillOutput activeHit = damageOutput("active-hit", SkillEffectTarget.FIRST);
        activeHit.setSkillId(normal.getId());
        putActionCharge(engine, normal, 1, 1);
        engine.putSkillOutputs(normal.getId(), java.util.List.of(activeHit));

        PassiveSkill passive = new PassiveSkill();
        passive.setId("splash-passive");
        passive.setName("splash-passive");
        passive.setCombatEvent(CombatEventType.AFTER_DEAL_ACTIVE_DMG);
        passive.setMaxTriggerPerBattle(1);
        SkillOutput splash = damageOutput("splash", SkillEffectTarget.ALL_ENEMY);
        splash.setPassiveSkillId(passive.getId());
        passive.setOutputs(java.util.List.of(splash));
        actor.getBattleCombatPassives().add(passive);

        engine.addUnit(actor);
        engine.addUnit(enemyA);
        engine.addUnit(enemyB);
        BattleResultVo result = engine.run();

        BattleEventVo cast = result.getEvents().stream()
                .filter(event -> "CAST".equals(event.getType()) && normal.getId().equals(event.getSkillId()))
                .findFirst().orElseThrow();
        assertThat(cast.getTargets()).containsExactly("enemy-a");
        assertThat(result.getEvents()).anySatisfy(event -> {
            assertThat(event.getType()).isEqualTo("PASSIVE_TRIGGER");
            assertThat(event.getSkillId()).isEqualTo(passive.getId());
            assertThat(event.getTriggerEvent()).isEqualTo(CombatEventType.AFTER_DEAL_ACTIVE_DMG.name());
        });
        assertThat(result.getEvents().stream()
                .filter(event -> "HIT".equals(event.getType()) && "enemy-b".equals(event.getTarget()))
                .map(BattleEventVo::getSkillId)).containsNull();
    }

    @Test
    void pulseKillSettlesBattleBeforeAnotherUnitCanAct() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        actor.setAction(2);
        actor.setBaseAction(2);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 10);

        ActiveSkill normal = new ActiveSkill();
        normal.setId("normal");
        normal.setName("normal");
        normal.setSkillType(ActiveSkillType.NORMAL);
        actor.getSkills().add(normal);
        normal.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
        putActionCharge(engine, normal, 1, 1);
        engine.putSkillOutputs(normal.getId(), java.util.List.of(damageOutput("hit", SkillEffectTarget.FIRST)));
        engine.addUnit(actor);
        engine.addUnit(enemy);

        BuffDef dot = new BuffDef();
        dot.setId("dot");
        dot.setName("dot");
        dot.setBuffKind(BuffKind.PULSE);
        dot.setStackMode(BuffStackMode.NONE);
        dot.setDurationAv(1);
        dot.setPulseEveryAv(1);
        dot.setPulseEffectType(SkillEffectType.DAMAGE);
        dot.setPulseTargetType(SkillEffectTarget.SELF);
        dot.setFormulaJson(TEN);
        engine.putBuffDef(dot);
        SkillV2OutputUnit.mountBuff(engine, actor, enemy, dot, "");

        BattleResultVo result = engine.run();

        assertThat(result.getOutcome()).isEqualTo("WIN");
        assertThat(result.getEvents()).noneMatch(event -> "CAST".equals(event.getType()));
    }

    @Test
    void bothSidesDeadAtSettlementPointIsDraw() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit ally = unit("ally", BattleSide.ALLY, 10);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 10);
        ally.setHp(0);
        enemy.setHp(0);
        engine.addUnit(ally);
        engine.addUnit(enemy);

        assertThat(engine.run().getOutcome()).isEqualTo("DRAW");
    }

    @Test
    void normalSkillChargesOnePointPerVaAndCastsAtUnitActionThreshold() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        actor.setAction(3);
        actor.setBaseAction(3);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 10);

        ActiveSkill normal = skill("normal", ActiveSkillType.NORMAL, 0);
        normal.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
        actor.getSkills().add(normal);
        putActionCharge(engine, normal, 1, 1);
        engine.putSkillOutputs(normal.getId(), java.util.List.of(damageOutput("normal-hit", SkillEffectTarget.FIRST)));
        engine.addUnit(actor);
        engine.addUnit(enemy);

        BattleResultVo result = engine.run();

        BattleEventVo cast = result.getEvents().stream()
                .filter(event -> "CAST".equals(event.getType()))
                .findFirst().orElseThrow();
        assertThat(cast.getT()).isEqualTo(3);
        assertThat(cast.getMax()).isEqualTo(3);
        assertThat(result.getEvents().stream()
                .filter(event -> "CHARGE".equals(event.getType()) && normal.getId().equals(event.getSkillId())))
                .hasSize(3);
    }

    @Test
    void customNormalUsesEditedNeedAndChargeRuleInsteadOfBuiltInDefaults() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        actor.setAction(3);
        actor.setBaseAction(3);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 10);
        ActiveSkill customNormal = skill("custom-normal", ActiveSkillType.NORMAL, 2);
        actor.getSkills().add(customNormal);
        putActionCharge(engine, customNormal, 3, 1);
        engine.putSkillOutputs(customNormal.getId(), java.util.List.of(damageOutput("hit", SkillEffectTarget.FIRST)));
        engine.addUnit(actor);
        engine.addUnit(enemy);

        BattleEventVo cast = engine.run().getEvents().stream()
                .filter(event -> "CAST".equals(event.getType()))
                .findFirst().orElseThrow();

        assertThat(cast.getT()).isEqualTo(6);
        assertThat(cast.getMax()).isEqualTo(2);
    }

    @Test
    void configuredChargeSkillCastsImmediatelyWhenVaChargeBecomesFull() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 10);
        ActiveSkill skill = skill("special", ActiveSkillType.SMALL, 2);
        actor.getSkills().add(skill);

        SkillCharge charge = new SkillCharge();
        charge.setConditionType(ChargeConditionType.ACTION_VALUE);
        charge.setScope(ChargeScope.GLOBAL);
        charge.setEveryActionValue(2);
        charge.setChargeGain(1);
        engine.putSkillMeta(skill.getId(), java.util.List.of(charge), java.util.List.of());
        engine.putSkillOutputs(skill.getId(), java.util.List.of(damageOutput("special-hit", SkillEffectTarget.FIRST)));
        engine.addUnit(actor);
        engine.addUnit(enemy);

        BattleEventVo cast = engine.run().getEvents().stream()
                .filter(event -> "CAST".equals(event.getType()))
                .findFirst().orElseThrow();
        assertThat(cast.getT()).isEqualTo(4);
        assertThat(cast.getSkillId()).isEqualTo(skill.getId());
    }

    @Test
    void eventChargeThatReachesFullCastsInTheSameVa() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit actor = unit("hero", BattleSide.ALLY, 100);
        actor.setAction(1);
        actor.setBaseAction(1);
        BattleRuntimeUnit enemy = unit("enemy", BattleSide.ENEMY, 100);
        ActiveSkill normal = skill("normal", ActiveSkillType.NORMAL, 0);
        normal.setMaxCastSkill(1);
        ActiveSkill followUp = skill("follow-up", ActiveSkillType.SMALL, 1);
        followUp.setMaxCastSkill(1);
        actor.getSkills().add(normal);
        actor.getSkills().add(followUp);
        normal.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
        putActionCharge(engine, normal, 1, 1);

        SkillCharge onCast = new SkillCharge();
        onCast.setConditionType(ChargeConditionType.SKILL_CHARGE);
        onCast.setSkillChargeEvent(SkillChargeEvent.CAST);
        onCast.setChargeGain(1);
        engine.putSkillMeta(followUp.getId(), java.util.List.of(onCast), java.util.List.of());
        engine.putSkillOutputs(normal.getId(), java.util.List.of(damageOutput("normal-hit", SkillEffectTarget.FIRST)));
        engine.putSkillOutputs(followUp.getId(), java.util.List.of(damageOutput("follow-hit", SkillEffectTarget.FIRST)));
        engine.addUnit(actor);
        engine.addUnit(enemy);

        java.util.List<BattleEventVo> casts = engine.run().getEvents().stream()
                .filter(event -> "CAST".equals(event.getType()))
                .toList();
        assertThat(casts).extracting(BattleEventVo::getSkillId).startsWith("normal", "follow-up");
        assertThat(casts.get(0).getT()).isEqualTo(1);
        assertThat(casts.get(1).getT()).isEqualTo(1);
    }

    private static SkillOutput damageOutput(String id, SkillEffectTarget target) {
        SkillOutput output = new SkillOutput();
        output.setId(id);
        output.setOutputKind(SkillOutputKind.EFFECT);
        output.setEffectType(SkillEffectType.DAMAGE);
        output.setTargetType(target);
        output.setFormulaJson(TEN);
        output.setHitSegments(1);
        output.setTriggerRate(100);
        return output;
    }

    private static ActiveSkill skill(String id, ActiveSkillType type, int need) {
        ActiveSkill skill = new ActiveSkill();
        skill.setId(id);
        skill.setName(id);
        skill.setSkillType(type);
        skill.setNeedCharge(need);
        return skill;
    }

    private static void putActionCharge(BattleEngine engine, ActiveSkill skill, int every, int gain) {
        SkillCharge charge = new SkillCharge();
        charge.setConditionType(ChargeConditionType.ACTION_VALUE);
        charge.setScope(ChargeScope.GLOBAL);
        charge.setEveryActionValue(every);
        charge.setChargeGain(gain);
        engine.putSkillMeta(skill.getId(), java.util.List.of(charge), java.util.List.of());
    }

    private static BattleRuntimeUnit unit(String id, BattleSide side, int hp) {
        BattleRuntimeUnit unit = new BattleRuntimeUnit();
        unit.setUnitId(id);
        unit.setSourceId(id);
        unit.setName(id);
        unit.setSide(side);
        unit.setMaxHp(hp);
        unit.setHp(hp);
        unit.setAtk(10);
        unit.setDef(0);
        unit.setAction(100);
        unit.setBaseAction(100);
        return unit;
    }
}
