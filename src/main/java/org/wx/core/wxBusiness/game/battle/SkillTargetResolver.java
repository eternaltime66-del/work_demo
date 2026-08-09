package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.unit.SkillTargetUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 通用角色选择模板解析（技能 V2）。
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
        if (type == SkillEffectTarget.SPECIAL || type.isEventExclusive()) {
            return List.of();
        }
        List<BattleRuntimeUnit> allies = living(all, BattleSide.ALLY);
        List<BattleRuntimeUnit> enemies = living(all, BattleSide.ENEMY);
        boolean casterAlly = caster.getSide() == BattleSide.ALLY;
        List<BattleRuntimeUnit> foes = casterAlly ? enemies : allies;
        List<BattleRuntimeUnit> friends = casterAlly ? allies : enemies;
        List<BattleRuntimeUnit> field = livingAll(all);

        return switch (type) {
            case SELF -> caster.alive() ? List.of(caster) : List.of();

            case ALLY_FIRST -> firstTarget(friends, casterAlly);
            case RANDOM_ALLY -> pickRandom(friends);
            case ALLY_MAX_HP -> extremeBy(friends, BattleRuntimeUnit::getHp, true);
            case ALLY_MIN_HP -> extremeBy(friends, BattleRuntimeUnit::getHp, false);
            case ALLY_MAX_MAX_HP -> extremeBy(friends, BattleRuntimeUnit::getMaxHp, true);
            case ALLY_MIN_MAX_HP -> extremeBy(friends, BattleRuntimeUnit::getMaxHp, false);
            case ALLY_MAX_HP_PCT -> extremeByDouble(friends, SkillTargetResolver::hpPct, true);
            case ALLY_MIN_HP_PCT -> extremeByDouble(friends, SkillTargetResolver::hpPct, false);
            case ALLY_MAX_ATK -> extremeBy(friends, BattleRuntimeUnit::getAtk, true);
            case ALLY_MIN_ATK -> extremeBy(friends, BattleRuntimeUnit::getAtk, false);
            case ALL_ALLY -> sortByFrontThenCol(friends, casterAlly);
            case ALLY_FRONT_ROW -> frontOrBackRow(friends, casterAlly, true);
            case ALLY_BACK_ROW -> frontOrBackRow(friends, casterAlly, false);

            case FIRST -> firstTarget(foes, !casterAlly);
            case RANDOM_ENEMY -> pickRandom(foes);
            case ENEMY_MAX_HP -> extremeBy(foes, BattleRuntimeUnit::getHp, true);
            case ENEMY_MIN_HP -> extremeBy(foes, BattleRuntimeUnit::getHp, false);
            case ENEMY_MAX_MAX_HP -> extremeBy(foes, BattleRuntimeUnit::getMaxHp, true);
            case ENEMY_MIN_MAX_HP -> extremeBy(foes, BattleRuntimeUnit::getMaxHp, false);
            case ENEMY_MAX_HP_PCT -> extremeByDouble(foes, SkillTargetResolver::hpPct, true);
            case ENEMY_MIN_HP_PCT -> extremeByDouble(foes, SkillTargetResolver::hpPct, false);
            case ENEMY_MAX_ATK -> extremeBy(foes, BattleRuntimeUnit::getAtk, true);
            case ENEMY_MIN_ATK -> extremeBy(foes, BattleRuntimeUnit::getAtk, false);
            case ALL_ENEMY -> sortByFrontThenCol(foes, !casterAlly);
            case FRONT_ROW -> frontOrBackRow(foes, !casterAlly, true);
            case BACK_ROW -> frontOrBackRow(foes, !casterAlly, false);

            case FIELD_MAX_HP -> extremeBy(field, BattleRuntimeUnit::getHp, true);
            case FIELD_MIN_HP -> extremeBy(field, BattleRuntimeUnit::getHp, false);
            case FIELD_MAX_MAX_HP -> extremeBy(field, BattleRuntimeUnit::getMaxHp, true);
            case FIELD_MIN_MAX_HP -> extremeBy(field, BattleRuntimeUnit::getMaxHp, false);
            case FIELD_MAX_HP_PCT -> extremeByDouble(field, SkillTargetResolver::hpPct, true);
            case FIELD_MIN_HP_PCT -> extremeByDouble(field, SkillTargetResolver::hpPct, false);
            case FIELD_MAX_ATK -> extremeBy(field, BattleRuntimeUnit::getAtk, true);
            case FIELD_MIN_ATK -> extremeBy(field, BattleRuntimeUnit::getAtk, false);
            case ALL_FIELD -> sortByFrontThenCol(field, true);
            case FIELD_FRONT_ROW -> mergeRows(frontOrBackRow(allies, true, true), frontOrBackRow(enemies, false, true));
            case FIELD_BACK_ROW -> mergeRows(frontOrBackRow(allies, true, false), frontOrBackRow(enemies, false, false));

            default -> List.of();
        };
    }

    public static List<BattleRuntimeUnit> resolveWithAnchor(
            SkillEffectTarget targetType,
            BattleRuntimeUnit owner,
            List<BattleRuntimeUnit> all,
            AnchorEvalContext anchor
    ) {
        SkillEffectTarget type = targetType == null ? SkillEffectTarget.SELF : targetType;
        if (type.isEventExclusive() || type.isPeriodicExclusive()) {
            if (type == SkillEffectTarget.EVENT_HIT_TARGETS
                    || type == SkillEffectTarget.EACH_DAMAGED_TARGET
                    || type == SkillEffectTarget.ANCHOR_HIT_TARGET) {
                if (anchor != null && anchor.getHitTargets() != null && !anchor.getHitTargets().isEmpty()) {
                    return anchor.getHitTargets().stream().filter(u -> u != null && u.alive()).toList();
                }
                BattleRuntimeUnit u = anchor != null ? anchor.getHitTarget() : null;
                return u != null && u.alive() ? List.of(u) : List.of();
            }
            BattleRuntimeUnit u = switch (type) {
                case EVENT_CASTER, ANCHOR_CASTER -> anchor != null ? anchor.getCaster() : null;
                case EVENT_DAMAGE_SOURCE, DAMAGE_SOURCE -> anchor != null ? anchor.getDamageSource() : null;
                case EVENT_PULSE_CASTER -> anchor != null ? anchor.getPulseCaster() : null;
                case EVENT_KILLER -> anchor != null ? anchor.getKiller() : null;
                case EVENT_KILLED -> anchor != null ? anchor.getKilled() : null;
                case SPECIFIC_TARGET -> anchor != null ? anchor.getSpecificTarget() : null;
                default -> null;
            };
            // 被击杀方允许已死亡；其余专属目标仍要求存活
            if (type == SkillEffectTarget.EVENT_KILLED) {
                return u != null ? List.of(u) : List.of();
            }
            return u != null && u.alive() ? List.of(u) : List.of();
        }
        return resolve(type, owner, all);
    }

    private static List<BattleRuntimeUnit> living(List<BattleRuntimeUnit> all, BattleSide side) {
        return all.stream().filter(u -> u.getSide() == side && u.alive()).collect(Collectors.toList());
    }

    private static List<BattleRuntimeUnit> livingAll(List<BattleRuntimeUnit> all) {
        return all.stream().filter(u -> u != null && u.alive()).collect(Collectors.toList());
    }

    private static List<BattleRuntimeUnit> pickRandom(List<BattleRuntimeUnit> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        return List.of(list.get(ThreadLocalRandom.current().nextInt(list.size())));
    }

    private static double hpPct(BattleRuntimeUnit u) {
        int max = Math.max(1, u.getMaxHp());
        return (double) u.getHp() / max;
    }

    private static List<BattleRuntimeUnit> firstTarget(List<BattleRuntimeUnit> units, boolean allySide) {
        if (units.isEmpty()) {
            return List.of();
        }
        List<BattleRuntimeUnit> sorted = new ArrayList<>(units);
        sorted.sort((a, b) -> {
            int front = SkillTargetUnit.compareFrontness(allySide, a.getPosRow(), b.getPosRow());
            if (front != 0) {
                return front;
            }
            return Integer.compare(a.getPosCol(), b.getPosCol());
        });
        return List.of(sorted.get(0));
    }

    private static List<BattleRuntimeUnit> frontOrBackRow(List<BattleRuntimeUnit> units, boolean allySide, boolean front) {
        if (units.isEmpty()) {
            return List.of();
        }
        Comparator<BattleRuntimeUnit> byFront = (a, b) ->
                SkillTargetUnit.compareFrontness(allySide, a.getPosRow(), b.getPosRow());
        BattleRuntimeUnit edge = front
                ? units.stream().min(byFront).orElse(null)
                : units.stream().max(byFront).orElse(null);
        if (edge == null) {
            return List.of();
        }
        int row = edge.getPosRow();
        return units.stream()
                .filter(u -> u.getPosRow() == row)
                .sorted(Comparator.comparingInt(BattleRuntimeUnit::getPosCol))
                .collect(Collectors.toList());
    }

    private static List<BattleRuntimeUnit> extremeBy(List<BattleRuntimeUnit> units, ToIntFunction<BattleRuntimeUnit> fn, boolean max) {
        if (units.isEmpty()) {
            return List.of();
        }
        BattleRuntimeUnit best = null;
        for (BattleRuntimeUnit u : units) {
            if (best == null) {
                best = u;
                continue;
            }
            int cmp = Integer.compare(fn.applyAsInt(u), fn.applyAsInt(best));
            if (max ? cmp > 0 : cmp < 0) {
                best = u;
            }
        }
        return best == null ? List.of() : List.of(best);
    }

    private static List<BattleRuntimeUnit> extremeByDouble(
            List<BattleRuntimeUnit> units, ToDoubleFunction<BattleRuntimeUnit> fn, boolean max) {
        if (units.isEmpty()) {
            return List.of();
        }
        BattleRuntimeUnit best = null;
        for (BattleRuntimeUnit u : units) {
            if (best == null) {
                best = u;
                continue;
            }
            int cmp = Double.compare(fn.applyAsDouble(u), fn.applyAsDouble(best));
            if (max ? cmp > 0 : cmp < 0) {
                best = u;
            }
        }
        return best == null ? List.of() : List.of(best);
    }

    private static List<BattleRuntimeUnit> sortByFrontThenCol(List<BattleRuntimeUnit> units, boolean allySide) {
        List<BattleRuntimeUnit> sorted = new ArrayList<>(units);
        sorted.sort((a, b) -> {
            int front = SkillTargetUnit.compareFrontness(allySide, a.getPosRow(), b.getPosRow());
            if (front != 0) {
                return front;
            }
            return Integer.compare(a.getPosCol(), b.getPosCol());
        });
        return sorted;
    }

    private static List<BattleRuntimeUnit> mergeRows(List<BattleRuntimeUnit> a, List<BattleRuntimeUnit> b) {
        List<BattleRuntimeUnit> out = new ArrayList<>();
        if (a != null) {
            out.addAll(a);
        }
        if (b != null) {
            out.addAll(b);
        }
        return out;
    }
}
