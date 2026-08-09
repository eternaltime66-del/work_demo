package org.wx.core.wxBusiness.game.battle;

import org.junit.jupiter.api.Test;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.DamageSourceKind;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.BuffStackMode;
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
    void attackSpeedChangeRecalculatesActionAndKeepsProgressRatio() {
        BattleEngine engine = new BattleEngine();
        BattleRuntimeUnit unit = unit("hero", BattleSide.ALLY, 100);
        unit.setBaseAction(100);
        unit.setAction(100);
        unit.setActionProgress(50);

        unit.setAtkSpeedAdd(100);
        engine.onAttackSpeedChanged(unit);
        assertThat(unit.getAction()).isEqualTo(50);
        assertThat(unit.getActionProgress()).isEqualTo(25);

        unit.setAtkSpeedAdd(0);
        engine.onAttackSpeedChanged(unit);
        assertThat(unit.getAction()).isEqualTo(100);
        assertThat(unit.getActionProgress()).isEqualTo(50);
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
