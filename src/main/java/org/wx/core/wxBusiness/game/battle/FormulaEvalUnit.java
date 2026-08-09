package org.wx.core.wxBusiness.game.battle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountDirection;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountScope;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadKey;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadRole;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * 公式 token JSON 求值。
 */
public final class FormulaEvalUnit {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FormulaEvalUnit() {
    }

    public static double eval(String formulaJson, BattleRuntimeUnit self, BattleRuntimeUnit target) {
        return eval(formulaJson, self, target, null, null);
    }

    public static double eval(String formulaJson, BattleRuntimeUnit self, BattleRuntimeUnit target, BattleStatBoard board) {
        return eval(formulaJson, self, target, board, null);
    }

    public static double eval(
            String formulaJson,
            BattleRuntimeUnit self,
            BattleRuntimeUnit target,
            BattleStatBoard board,
            AnchorEvalContext ctx
    ) {
        List<Map<String, Object>> tokens = parse(formulaJson);
        if (tokens.isEmpty()) {
            return 0;
        }
        List<String> rpn = toRpn(tokens, self, target, board, ctx);
        return evalRpn(rpn);
    }

    private static List<Map<String, Object>> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private static double readUnitAttr(BattleRuntimeUnit u, String key) {
        return readUnitAttr(u, key, null);
    }

    private static double readUnitAttr(BattleRuntimeUnit u, String key, BattleStatBoard board) {
        if (u == null || key == null) {
            return 0;
        }
        FormulaReadKey k;
        try {
            k = FormulaReadKey.valueOf(key);
        } catch (Exception e) {
            return 0;
        }
        String id = u.getUnitId();
        return switch (k) {
            case MAX_HP -> u.getMaxHp();
            case HP -> u.getHp();
            case ATK -> u.getAtk();
            case DEF -> u.getDef();
            case ACTION -> u.getAction();
            case ELAPSED_ACTION -> board == null ? 0 : board.getElapsedActionValue();
            case DEAL_DMG_AMOUNT -> board == null ? 0 : board.getDealDamageAmount(id);
            case TAKE_DMG_AMOUNT -> board == null ? 0 : board.getReceiveDamageAmount(id);
            case DEAL_DMG_COUNT -> board == null ? 0 : board.getDealDamageCount(id);
            case TAKE_DMG_COUNT -> board == null ? 0 : board.getReceiveDamageCount(id);
            case DEAL_HEAL_AMOUNT -> board == null ? 0 : board.getDealHealAmount(id);
            case TAKE_HEAL_AMOUNT -> board == null ? 0 : board.getReceiveHealAmount(id);
            case DEAL_HEAL_COUNT -> board == null ? 0 : board.getDealHealCount(id);
            case TAKE_HEAL_COUNT -> board == null ? 0 : board.getReceiveHealCount(id);
            case ELEMENT_DMG_BONUS -> u.getElementDmgBonus();
            case ELEMENT_PHYS_BONUS -> u.getElementPhysBonus();
            case ELEMENT_POISON_BONUS -> u.getElementPoisonBonus();
            case ELEMENT_IGNITE_BONUS -> u.getElementIgniteBonus();
            case ELEMENT_FREEZE_BONUS -> u.getElementFreezeBonus();
            case ELEMENT_SHOCK_BONUS -> u.getElementShockBonus();
            case ELEMENT_BURN_BONUS -> u.getElementBurnBonus();
        };
    }

    private static BattleRuntimeUnit unitOf(
            FormulaReadRole rr,
            BattleRuntimeUnit self,
            BattleRuntimeUnit target,
            AnchorEvalContext ctx
    ) {
        if (rr == null || rr == FormulaReadRole.SELF) {
            return self;
        }
        return switch (rr) {
            case EACH_TARGET -> target;
            case ANCHOR_HIT_TARGET, EACH_DAMAGED_TARGET ->
                    ctx != null && ctx.getHitTarget() != null ? ctx.getHitTarget() : target;
            case ANCHOR_CASTER ->
                    ctx != null && ctx.getCaster() != null ? ctx.getCaster() : self;
            case DAMAGE_SOURCE ->
                    ctx != null && ctx.getDamageSource() != null ? ctx.getDamageSource() : self;
            case SPECIFIC_TARGET ->
                    ctx != null && ctx.getSpecificTarget() != null ? ctx.getSpecificTarget() : target;
            default -> self;
        };
    }

    private static List<BattleRuntimeUnit> aggregateUnits(FormulaReadRole rr, BattleRuntimeUnit self, AnchorEvalContext ctx) {
        if (self == null || ctx == null || ctx.getAllUnits() == null) {
            return List.of();
        }
        BattleSide selfSide = self.getSide();
        BattleSide want = rr == FormulaReadRole.ALL_ALLY ? selfSide
                : (selfSide == BattleSide.ALLY ? BattleSide.ENEMY : BattleSide.ALLY);
        return ctx.getAllUnits().stream()
                .filter(u -> u != null && u.alive() && u.getSide() == want)
                .collect(Collectors.toList());
    }

    private static double sumOver(List<BattleRuntimeUnit> units, ToDoubleFunction<BattleRuntimeUnit> fn) {
        double s = 0;
        for (BattleRuntimeUnit u : units) {
            s += fn.applyAsDouble(u);
        }
        return s;
    }

    private static double tokenValue(
            Map<String, Object> t,
            BattleRuntimeUnit self,
            BattleRuntimeUnit target,
            BattleStatBoard board,
            AnchorEvalContext ctx
    ) {
        String kind = str(t.get("kind"));
        if ("OP".equals(kind)) {
            return Double.NaN;
        }
        String mode = str(t.get("paramMode"));
        if ("LITERAL".equals(mode)) {
            try {
                return Double.parseDouble(str(t.get("value")));
            } catch (Exception e) {
                return 0;
            }
        }

        FormulaReadRole rr = FormulaReadRole.SELF;
        try {
            String role = str(t.get("readRole"));
            if (!role.isEmpty()) {
                rr = FormulaReadRole.valueOf(role);
            }
        } catch (Exception ignored) {
        }

        if (rr == FormulaReadRole.GLOBAL) {
            if (board == null) {
                return 0;
            }
            return Math.max(0, board.getElapsedActionValue());
        }

        String category = str(t.get("readCategory"));
        if ("ANCHOR".equals(category)) {
            return readAnchorMetric(ctx, str(t.get("anchorMetric")));
        }

        if (rr.isAggregateRole()) {
            List<BattleRuntimeUnit> units = aggregateUnits(rr, self, ctx);
            return switch (category.isEmpty() ? "ATTR" : category) {
                case "ATTR" -> sumOver(units, u -> readUnitAttr(u, str(t.get("readKey")), board));
                case "DAMAGE" -> sumOver(units, u -> readDamage(u, board, str(t.get("damageSide")), str(t.get("damageMetric"))));
                case "SKILL" -> sumOver(units, u -> readSkillCount(u, board, t));
                default -> sumOver(units, u -> readUnitAttr(u, str(t.get("readKey")), board));
            };
        }

        BattleRuntimeUnit u = unitOf(rr, self, target, ctx);
        if (category.isEmpty()) {
            return readUnitAttr(u, str(t.get("readKey")), board);
        }

        return switch (category) {
            case "ATTR" -> readUnitAttr(u, str(t.get("readKey")), board);
            case "DAMAGE" -> readDamage(u, board, str(t.get("damageSide")), str(t.get("damageMetric")));
            case "SKILL" -> readSkillCount(u, board, t);
            default -> readUnitAttr(u, str(t.get("readKey")), board);
        };
    }

    private static double readAnchorMetric(AnchorEvalContext ctx, String metric) {
        if (ctx == null) {
            return 0;
        }
        if ("SKILL_DAMAGE".equals(metric)) {
            return Math.max(0, ctx.getSkillDamage());
        }
        return Math.max(0, ctx.getHitDamage());
    }

    private static double readDamage(BattleRuntimeUnit u, BattleStatBoard board, String side, String metric) {
        if (u == null || board == null) {
            return 0;
        }
        String id = u.getUnitId();
        boolean deal = !"RECEIVE".equals(side);
        if ("ACTIVE_COUNT".equals(metric)) {
            return deal ? board.getActiveDealDamageCount(id) : board.getActiveReceiveDamageCount(id);
        }
        boolean amount = !"COUNT".equals(metric);
        if (deal) {
            return amount ? board.getDealDamageAmount(id) : board.getDealDamageCount(id);
        }
        return amount ? board.getReceiveDamageAmount(id) : board.getReceiveDamageCount(id);
    }

    private static double readSkillCount(BattleRuntimeUnit u, BattleStatBoard board, Map<String, Object> t) {
        if (u == null || board == null) {
            return 0;
        }
        SkillCountDirection dir = "RECEIVE".equals(str(t.get("skillMetric")))
                ? SkillCountDirection.RECEIVE
                : SkillCountDirection.CAST;
        String match = str(t.get("skillMatch"));
        if ("ANY_TYPE".equals(match)) {
            ActiveSkillType type = null;
            try {
                String ts = str(t.get("matchSkillType"));
                if (!ts.isEmpty()) {
                    type = ActiveSkillType.valueOf(ts);
                }
            } catch (Exception ignored) {
            }
            if (type == null) {
                return 0;
            }
            return board.getSkillCount(u.getUnitId(), dir, SkillCountScope.SKILL_TYPE, null, type);
        }
        if ("ANY_SCHOOL".equals(match)) {
            String school = SkillSchoolUnit.normalizeSchool(str(t.get("matchSkillSchool")));
            return board.getSkillCount(u.getUnitId(), dir, SkillCountScope.SKILL_SCHOOL, null, null, school, null);
        }
        if ("ANY_ELEMENT".equals(match)) {
            DamageElement el = null;
            try {
                String es = str(t.get("matchDamageElement"));
                if (!es.isEmpty()) {
                    el = DamageElement.valueOf(es);
                }
            } catch (Exception ignored) {
            }
            if (el == null) {
                el = DamageElement.PHYSICAL;
            }
            return board.getSkillCount(u.getUnitId(), dir, SkillCountScope.SKILL_ELEMENT, null, null, null, el);
        }
        if ("SPECIFIC".equals(match)) {
            String skillId = str(t.get("matchSkillId"));
            if (skillId.isEmpty()) {
                return 0;
            }
            return board.getSkillCount(u.getUnitId(), dir, SkillCountScope.SPECIFIC_SKILL, skillId, null);
        }
        return board.getSkillCount(u.getUnitId(), dir, SkillCountScope.ANY, null, null);
    }

    private static List<String> toRpn(
            List<Map<String, Object>> tokens,
            BattleRuntimeUnit self,
            BattleRuntimeUnit target,
            BattleStatBoard board,
            AnchorEvalContext ctx
    ) {
        List<String> out = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();
        for (Map<String, Object> t : tokens) {
            String kind = str(t.get("kind"));
            if ("OP".equals(kind)) {
                String op = str(t.get("op"));
                if ("(".equals(op)) {
                    ops.push(op);
                } else if (")".equals(op)) {
                    while (!ops.isEmpty() && !"(".equals(ops.peek())) {
                        out.add(ops.pop());
                    }
                    if (!ops.isEmpty()) {
                        ops.pop();
                    }
                } else {
                    while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(op)) {
                        out.add(ops.pop());
                    }
                    ops.push(op);
                }
            } else {
                out.add(String.valueOf(tokenValue(t, self, target, board, ctx)));
            }
        }
        while (!ops.isEmpty()) {
            out.add(ops.pop());
        }
        return out;
    }

    private static int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }

    private static double evalRpn(List<String> rpn) {
        Deque<Double> st = new ArrayDeque<>();
        for (String x : rpn) {
            if ("+".equals(x) || "-".equals(x) || "*".equals(x) || "/".equals(x)) {
                double b = st.isEmpty() ? 0 : st.pop();
                double a = st.isEmpty() ? 0 : st.pop();
                st.push(switch (x) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> b == 0 ? 0 : a / b;
                    default -> 0d;
                });
            } else {
                try {
                    st.push(Double.parseDouble(x));
                } catch (Exception e) {
                    st.push(0d);
                }
            }
        }
        return st.isEmpty() ? 0 : st.pop();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
