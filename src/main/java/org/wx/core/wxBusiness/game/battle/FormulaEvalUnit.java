package org.wx.core.wxBusiness.game.battle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadKey;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadRole;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * 公式 token JSON 求值（支持 + - * / ( )、手动值、自己/每个目标 属性读取）
 */
public final class FormulaEvalUnit {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FormulaEvalUnit() {
    }

    public static double eval(String formulaJson, BattleRuntimeUnit self, BattleRuntimeUnit target) {
        List<Map<String, Object>> tokens = parse(formulaJson);
        if (tokens.isEmpty()) {
            return 0;
        }
        List<String> rpn = toRpn(tokens, self, target);
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

    private static double readStat(BattleRuntimeUnit u, String key) {
        if (u == null || key == null) {
            return 0;
        }
        FormulaReadKey k;
        try {
            k = FormulaReadKey.valueOf(key);
        } catch (Exception e) {
            return 0;
        }
        return switch (k) {
            case MAX_HP -> u.getMaxHp();
            case HP -> u.getHp();
            case ATK -> u.getAtk();
            case DEF -> u.getDef();
            case ACTION -> u.getAction();
            case ELAPSED_ACTION -> 0;
        };
    }

    private static double tokenValue(Map<String, Object> t, BattleRuntimeUnit self, BattleRuntimeUnit target) {
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
        String role = str(t.get("readRole"));
        FormulaReadRole rr = FormulaReadRole.SELF;
        try {
            if (!role.isEmpty()) {
                rr = FormulaReadRole.valueOf(role);
            }
        } catch (Exception ignored) {
        }
        BattleRuntimeUnit u = rr == FormulaReadRole.EACH_TARGET ? target : self;
        return readStat(u, str(t.get("readKey")));
    }

    private static List<String> toRpn(List<Map<String, Object>> tokens, BattleRuntimeUnit self, BattleRuntimeUnit target) {
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
                out.add(String.valueOf(tokenValue(t, self, target)));
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
