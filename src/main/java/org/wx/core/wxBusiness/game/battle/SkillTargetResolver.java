package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.unit.SkillTargetUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 按布阵约定挑选技能目标
 */
public final class SkillTargetResolver {

    private SkillTargetResolver() {
    }

    public static List<BattleRuntimeUnit> resolve(SkillEffectTarget targetType, BattleRuntimeUnit caster,
                                                  List<BattleRuntimeUnit> all) {
        if (caster == null || all == null) {
            return List.of();
        }
        SkillEffectTarget type = targetType == null ? SkillEffectTarget.FIRST : targetType;
        List<BattleRuntimeUnit> allies = living(all, BattleSide.ALLY);
        List<BattleRuntimeUnit> enemies = living(all, BattleSide.ENEMY);
        boolean casterAlly = caster.getSide() == BattleSide.ALLY;
        List<BattleRuntimeUnit> foes = casterAlly ? enemies : allies;
        List<BattleRuntimeUnit> friends = casterAlly ? allies : enemies;

        return switch (type) {
            case SELF -> caster.alive() ? List.of(caster) : List.of();
            case FIRST -> firstTarget(foes, !casterAlly);
            case FRONT_ROW -> frontOrBackRow(foes, !casterAlly, true);
            case BACK_ROW -> frontOrBackRow(foes, !casterAlly, false);
            case RANDOM_ENEMY -> foes.isEmpty() ? List.of()
                    : List.of(foes.get(ThreadLocalRandom.current().nextInt(foes.size())));
            case ENEMY_MAX_HP -> extremeHp(foes, true);
            case ENEMY_MIN_HP -> extremeHp(foes, false);
            case ALLY_MIN_HP -> extremeHp(friends, false);
            case ALL_ENEMY -> sortByFrontThenCol(foes, !casterAlly);
            case ALL_ALLY -> sortByFrontThenCol(friends, casterAlly);
        };
    }

    private static List<BattleRuntimeUnit> living(List<BattleRuntimeUnit> all, BattleSide side) {
        return all.stream().filter(u -> u.getSide() == side && u.alive()).collect(Collectors.toList());
    }

    /** 首目标：前排起，每排从左到右 */
    private static List<BattleRuntimeUnit> firstTarget(List<BattleRuntimeUnit> foes, boolean foeIsMonster) {
        if (foes.isEmpty()) {
            return List.of();
        }
        List<BattleRuntimeUnit> sorted = new ArrayList<>(foes);
        sorted.sort((a, b) -> {
            int front = SkillTargetUnit.compareFrontness(!foeIsMonster, a.getPosRow(), b.getPosRow());
            if (front != 0) {
                return front;
            }
            return Integer.compare(a.getPosCol(), b.getPosCol());
        });
        return List.of(sorted.get(0));
    }

    private static List<BattleRuntimeUnit> frontOrBackRow(List<BattleRuntimeUnit> foes, boolean foeIsMonster, boolean front) {
        if (foes.isEmpty()) {
            return List.of();
        }
        Comparator<BattleRuntimeUnit> byFront = (a, b) ->
                SkillTargetUnit.compareFrontness(!foeIsMonster, a.getPosRow(), b.getPosRow());
        BattleRuntimeUnit edge = front
                ? foes.stream().min(byFront).orElse(null)
                : foes.stream().max(byFront).orElse(null);
        if (edge == null) {
            return List.of();
        }
        int row = edge.getPosRow();
        return foes.stream()
                .filter(u -> u.getPosRow() == row)
                .sorted(Comparator.comparingInt(BattleRuntimeUnit::getPosCol))
                .collect(Collectors.toList());
    }

    private static List<BattleRuntimeUnit> extremeHp(List<BattleRuntimeUnit> units, boolean max) {
        if (units.isEmpty()) {
            return List.of();
        }
        Comparator<BattleRuntimeUnit> cmp = Comparator.comparingInt(BattleRuntimeUnit::getHp);
        BattleRuntimeUnit hit = max ? units.stream().max(cmp).orElse(null) : units.stream().min(cmp).orElse(null);
        return hit == null ? List.of() : List.of(hit);
    }

    /** 全部目标：前排优先，同行从左到右 */
    private static List<BattleRuntimeUnit> sortByFrontThenCol(List<BattleRuntimeUnit> units, boolean sideIsAlly) {
        if (units.isEmpty()) {
            return List.of();
        }
        List<BattleRuntimeUnit> sorted = new ArrayList<>(units);
        sorted.sort((a, b) -> {
            int front = SkillTargetUnit.compareFrontness(sideIsAlly, a.getPosRow(), b.getPosRow());
            if (front != 0) {
                return front;
            }
            return Integer.compare(a.getPosCol(), b.getPosCol());
        });
        return sorted;
    }
}
