package org.wx.core.wxBusiness.game.battle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveCombatEffect;
import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadKey;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadRole;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.vo.PassiveDescVo;
import org.wx.core.wxBusiness.game.entity.vo.SkillDescVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 技能 / 被动 / 公式 → 人性化中文文案。
 */
public final class GameDescUnit {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GameDescUnit() {
    }

    public static String activeSkillTypeLabel(ActiveSkillType type) {
        if (type == null) {
            return "技能";
        }
        return switch (type) {
            case NORMAL -> "普攻";
            case ULTIMATE -> "大招";
            case SMALL -> "小技能";
        };
    }

    public static String formatFormula(String formulaJson) {
        if (!StringUtils.hasText(formulaJson)) {
            return "";
        }
        try {
            JsonNode arr = MAPPER.readTree(formulaJson);
            if (!arr.isArray() || arr.isEmpty()) {
                return "";
            }
            List<String> parts = new ArrayList<>();
            for (JsonNode n : arr) {
                if (n == null || n.isNull()) {
                    continue;
                }
                String kind = text(n, "kind");
                if ("OP".equals(kind)) {
                    parts.add(opLabel(text(n, "op")));
                    continue;
                }
                String mode = text(n, "paramMode");
                if ("LITERAL".equals(mode) || (!StringUtils.hasText(mode) && n.has("value") && !"READ".equals(mode))) {
                    String v = text(n, "value");
                    if (StringUtils.hasText(v)) {
                        parts.add(v);
                    }
                    continue;
                }
                if ("READ".equals(mode) || StringUtils.hasText(text(n, "readKey")) || StringUtils.hasText(text(n, "readCategory"))) {
                    parts.add(readTokenLabel(n));
                }
            }
            return String.join(" ", parts);
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String effectLine(SkillEffect effect) {
        if (effect == null) {
            return "";
        }
        SkillEffectType type = effect.getEffectType();
        String target = targetLabel(effect.getTargetType());
        String formula = formatFormula(effect.getFormulaJson());
        String formulaPart = StringUtils.hasText(formula) ? "（" + formula + "）" : "";
        String seg = "";
        if (effect.getHitSegments() != null && effect.getHitSegments() > 1) {
            seg = "，分" + effect.getHitSegments() + "段结算";
        }
        String ratePart = triggerRateText(effect.getTriggerRate());
        String durPart = durationAvText(effect.getDurationAv());
        String prefix = StringUtils.hasText(effect.getName()) ? "【" + effect.getName() + "】" : "";
        if (type == SkillEffectType.HEAL) {
            return prefix + "为" + target + "恢复生命" + formulaPart + seg + ratePart;
        }
        if (type == SkillEffectType.ATTR_MODIFY) {
            String attr = attrKeyLabel(effect.getAttrKey());
            String dir = effect.getAttrDir() == AttrModifyDirection.DECREASE ? "减少" : "增加";
            return prefix + "使" + target + "的" + attr + dir + formulaPart + seg + ratePart + durPart;
        }
        // DAMAGE default
        return prefix + "对" + target + "造成伤害" + formulaPart + seg + ratePart;
    }

    /** 非 100% 时追加「，概率N%」 */
    private static String triggerRateText(Integer triggerRate) {
        if (triggerRate == null || triggerRate >= 100) {
            return "";
        }
        int rate = Math.max(0, triggerRate);
        return "，概率" + rate + "%";
    }

    /** durationAv &gt; 0 时追加「，持续N行动值」 */
    private static String durationAvText(Integer durationAv) {
        if (durationAv == null || durationAv <= 0) {
            return "";
        }
        return "，持续" + durationAv + "行动值";
    }

    public static String chargeLine(SkillCharge charge, Function<String, String> skillNameFn) {
        if (charge == null) {
            return "";
        }
        int gain = charge.getChargeGain() != null ? charge.getChargeGain() : 0;
        String namePrefix = StringUtils.hasText(charge.getName()) ? "【" + charge.getName() + "】" : "";
        if (charge.getConditionType() == ChargeConditionType.ACTION_VALUE) {
            int every = charge.getEveryActionValue() != null ? charge.getEveryActionValue() : 0;
            return namePrefix + "每经过" + every + "行动值，增加" + gain + "点充能";
        }
        // SKILL_CHARGE
        String event = charge.getSkillChargeEvent() == SkillChargeEvent.RECEIVE ? "受到" : "释放";
        String match = skillChargeMatchText(charge, skillNameFn);
        return namePrefix + event + match + "时，增加" + gain + "点充能";
    }

    public static String needChargeText(ActiveSkill skill) {
        if (skill == null) {
            return "";
        }
        if (skill.getSkillType() == ActiveSkillType.NORMAL) {
            return "普攻无需充能";
        }
        if (skill.getNeedChargeMode() == NeedChargeMode.SELF_BASE_ACTION) {
            return "释放所需充能：自己的基础行动值";
        }
        int n = skill.getNeedCharge() != null ? skill.getNeedCharge() : 0;
        return "释放所需充能：" + n;
    }

    public static String castLimitText(ActiveSkill skill) {
        if (skill == null) {
            return "";
        }
        Integer max = skill.getMaxCastSkill();
        if (max == null || max <= 0) {
            return "本技能释放次数不限";
        }
        return "本技能最多释放" + max + "次";
    }

    public static SkillDescVo describeActiveSkill(
            ActiveSkill skill,
            List<SkillCharge> charges,
            List<SkillEffect> effects,
            Function<String, String> skillNameFn
    ) {
        SkillDescVo vo = new SkillDescVo();
        if (skill == null) {
            return vo;
        }
        vo.setId(skill.getId());
        vo.setName(skill.getName());
        vo.setSkillType(skill.getSkillType() != null ? skill.getSkillType().name() : null);
        vo.setSkillTypeLabel(activeSkillTypeLabel(skill.getSkillType()));
        vo.setNeedChargeText(needChargeText(skill));
        vo.setCastLimitText(castLimitText(skill));

        List<String> chargeTexts = new ArrayList<>();
        if (charges != null) {
            for (SkillCharge c : charges) {
                String line = chargeLine(c, skillNameFn);
                if (StringUtils.hasText(line)) {
                    chargeTexts.add(line);
                }
            }
        }
        vo.setChargeTexts(chargeTexts);

        List<String> effectTexts = new ArrayList<>();
        if (effects != null) {
            for (SkillEffect e : effects) {
                String line = effectLine(e);
                if (StringUtils.hasText(line)) {
                    effectTexts.add(line);
                }
            }
        }
        vo.setEffectTexts(effectTexts);
        vo.setSummary(joinSkillSummary(vo));
        return vo;
    }

    public static String passiveEffectLine(PassiveEffect effect) {
        if (effect == null || effect.getAttrKey() == null) {
            return "";
        }
        PassiveEffectAttrKey key = effect.getAttrKey();
        boolean dec = effect.getAttrDir() == AttrModifyDirection.DECREASE;
        String dir = dec ? "减少" : "增加";
        BigDecimal num = effect.getValueNum() != null ? effect.getValueNum() : BigDecimal.ZERO;
        String n = stripTrailingZeros(num);
        return switch (key.getStackMode()) {
            case ADD_FLAT -> "自己的" + key.getLabel() + dir + n + "点";
            case ADD_PERCENT -> {
                if (key.isFinalRatio()) {
                    yield key.getLabel() + "比例" + dir + n + "%（加法；总属性=合计×最终比例）";
                }
                yield key.getLabel() + dir + n + "%（加法叠加）";
            }
            case MULT_PERCENT -> {
                String factor = dec ? "1-" + n + "%" : "1+" + n + "%";
                yield key.getLabel() + dir + n + "%（叠乘 " + factor + "）";
            }
        };
    }

    public static String passiveConditionLine(
            PassiveCondition cond,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn
    ) {
        if (cond == null || cond.getConditionType() == null) {
            return "";
        }
        PassiveConditionType type = cond.getConditionType();
        return switch (type) {
            case EQUIP_ITEM -> "装备了「" + nameOrId(itemNameFn, cond.getRefItemId()) + "」";
            case EQUIP_ITEM_TYPE -> {
                ItemType it = cond.getRefItemType();
                yield "装备了" + (it != null ? it.label() : "指定类型装备");
            }
            case EQUIP_SKILL -> "装备了技能「" + nameOrId(skillNameFn, cond.getRefSkillId()) + "」";
            case EQUIP_SKILL_TYPE -> "装备了" + activeSkillTypeLabel(cond.getRefSkillType()) + "类技能";
            case FORMULA_COMPARE -> {
                String left = formatFormula(cond.getLeftFormulaJson());
                String right = formatFormula(cond.getRightFormulaJson());
                CompareOp op = cond.getCompareOp();
                String opText = op != null ? op.label() : "比较";
                yield "当（" + (StringUtils.hasText(left) ? left : "?") + "）"
                        + opText
                        + "（" + (StringUtils.hasText(right) ? right : "?") + "）";
            }
        };
    }

    public static PassiveDescVo describePassive(
            PassiveSkill skill,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn
    ) {
        List<PassiveCondition> conditions = skill != null ? skill.getConditions() : null;
        List<PassiveEffect> effects = skill != null ? skill.getEffects() : null;
        return describePassive(skill, conditions, effects, itemNameFn, skillNameFn);
    }

    public static PassiveDescVo describePassive(
            PassiveSkill skill,
            List<PassiveCondition> conditions,
            List<PassiveEffect> effects,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn
    ) {
        PassiveDescVo vo = new PassiveDescVo();
        if (skill == null) {
            return vo;
        }
        vo.setId(skill.getId());
        vo.setName(skill.getName());
        vo.setPassiveType(skill.getPassiveType() != null ? skill.getPassiveType().name() : null);
        vo.setPassiveTypeLabel(passiveTypeLabel(skill.getPassiveType()));

        List<String> condTexts = new ArrayList<>();
        if (skill.getConditionMode() == PassiveConditionMode.UNLIMITED
                || skill.getConditionMode() == null) {
            vo.setConditionText("始终生效");
        } else {
            if (conditions != null) {
                for (PassiveCondition c : conditions) {
                    String line = passiveConditionLine(c, itemNameFn, skillNameFn);
                    if (StringUtils.hasText(line)) {
                        condTexts.add(line);
                    }
                }
            }
            vo.setConditionText(condTexts.isEmpty() ? "需满足条件（未配置）" : "当满足：" + String.join("，且", condTexts));
        }
        vo.setConditionTexts(condTexts);

        List<String> effectTexts = new ArrayList<>();
        if (skill.getPassiveType() == PassiveSkillType.IN_ANCHOR) {
            if (skill.getAnchorType() != null) {
                effectTexts.add("锚点：" + skill.getAnchorType().getLabel()
                        + anchorMatchSuffix(skill, skillNameFn));
            }
            appendCombatEffectTexts(effectTexts, skill.getCombatEffects());
        } else if (skill.getPassiveType() == PassiveSkillType.IN_PERIODIC
                || skill.getPassiveType() == PassiveSkillType.IN_SUSTAINED) {
            boolean sustained = skill.getPassiveType() == PassiveSkillType.IN_SUSTAINED;
            if (skill.getPeriodicTriggerMode() != null) {
                effectTexts.add((sustained ? "判定：" : "触发：") + skill.getPeriodicTriggerMode().getLabel());
            }
            String left = formatFormula(skill.getLeftFormulaJson());
            String right = formatFormula(skill.getRightFormulaJson());
            String op = skill.getCompareOp() != null ? skill.getCompareOp().symbol() : "?";
            if (StringUtils.hasText(left) || StringUtils.hasText(right)) {
                effectTexts.add((sustained ? "当 " : "每当 ")
                        + (StringUtils.hasText(left) ? left : "?")
                        + " " + op + " " + (StringUtils.hasText(right) ? right : "?")
                        + (sustained ? " 时生效，不满足则取消" : ""));
            }
            if (sustained) {
                effectTexts.add("持续效果：条件维持期间生效");
            } else {
                Integer max = skill.getMaxTriggerPerBattle();
                if (max != null && max > 0) {
                    effectTexts.add("本场最多触发 " + max + " 次");
                } else {
                    effectTexts.add("本场触发次数不限");
                }
            }
            appendCombatEffectTexts(effectTexts, skill.getCombatEffects());
        } else if (effects != null) {
            for (PassiveEffect e : effects) {
                String line = passiveEffectLine(e);
                if (StringUtils.hasText(line)) {
                    effectTexts.add(line);
                }
            }
        }
        vo.setEffectTexts(effectTexts);
        vo.setSummary(joinPassiveSummary(vo));
        return vo;
    }

    public static String combatEffectLine(PassiveCombatEffect effect) {
        if (effect == null || effect.getEffectType() == null) {
            return "";
        }
        SkillEffectType type = effect.getEffectType();
        String target = targetLabel(effect.getTargetType());
        String formula = formatFormula(effect.getFormulaJson());
        String formulaPart = StringUtils.hasText(formula) ? "（" + formula + "）" : "";
        String seg = "";
        if (effect.getHitSegments() != null && effect.getHitSegments() > 1) {
            seg = "，分" + effect.getHitSegments() + "段结算";
        }
        String ratePart = triggerRateText(effect.getTriggerRate());
        String durPart = durationAvText(effect.getDurationAv());
        String prefix = StringUtils.hasText(effect.getName()) ? "【" + effect.getName() + "】" : "";
        if (type == SkillEffectType.HEAL) {
            return prefix + "为" + target + "恢复生命" + formulaPart + seg + ratePart;
        }
        if (type == SkillEffectType.ATTR_MODIFY) {
            String attr = attrKeyLabel(effect.getAttrKey());
            String dir = effect.getAttrDir() == AttrModifyDirection.DECREASE ? "减少" : "增加";
            return prefix + "使" + target + "的" + attr + dir + formulaPart + seg + ratePart + durPart;
        }
        return prefix + "对" + target + "造成伤害" + formulaPart + seg + ratePart;
    }

    private static void appendCombatEffectTexts(List<String> effectTexts, List<PassiveCombatEffect> combatEffects) {
        if (combatEffects == null || effectTexts == null) {
            return;
        }
        for (PassiveCombatEffect e : combatEffects) {
            String line = combatEffectLine(e);
            if (StringUtils.hasText(line)) {
                effectTexts.add(line);
            }
        }
    }

    private static String anchorMatchSuffix(PassiveSkill skill, Function<String, String> skillNameFn) {
        PassiveAnchorType t = skill.getAnchorType();
        if (t == null || !t.needsSkillMatch()) {
            return "";
        }
        SkillChargeMatchMode mode = skill.getSkillMatchMode();
        if (mode == null || mode == SkillChargeMatchMode.ANY) {
            return " · 任意技能";
        }
        if (mode == SkillChargeMatchMode.ANY_TYPE) {
            return " · " + activeSkillTypeLabel(skill.getRefSkillType()) + "类";
        }
        return " · 「" + nameOrId(skillNameFn, skill.getRefSkillId()) + "」";
    }

    public static String atkSpeedText(BigDecimal up, BigDecimal down) {
        BigDecimal u = up != null ? up : BigDecimal.ZERO;
        BigDecimal d = down != null ? down : BigDecimal.ZERO;
        if (u.compareTo(BigDecimal.ZERO) > 0) {
            return stripTrailingZeros(u) + "%";
        }
        if (d.compareTo(BigDecimal.ZERO) > 0) {
            return "-" + stripTrailingZeros(d) + "%";
        }
        return "无变化";
    }

    private static String joinSkillSummary(SkillDescVo vo) {
        List<String> blocks = new ArrayList<>();
        String head = (vo.getSkillTypeLabel() != null ? "【" + vo.getSkillTypeLabel() + "】" : "")
                + (StringUtils.hasText(vo.getName()) ? vo.getName() : "未命名技能");
        blocks.add(head);
        if (StringUtils.hasText(vo.getNeedChargeText())) {
            blocks.add(vo.getNeedChargeText());
        }
        if (StringUtils.hasText(vo.getCastLimitText()) && vo.getCastLimitText().contains("最多")) {
            blocks.add(vo.getCastLimitText());
        }
        if (vo.getChargeTexts() != null && !vo.getChargeTexts().isEmpty()) {
            blocks.add("充能：" + String.join("；", vo.getChargeTexts()));
        }
        if (vo.getEffectTexts() != null && !vo.getEffectTexts().isEmpty()) {
            blocks.add("效果：" + String.join("；", vo.getEffectTexts()));
        } else {
            blocks.add("效果：暂无");
        }
        return String.join("\n", blocks);
    }

    private static String joinPassiveSummary(PassiveDescVo vo) {
        List<String> blocks = new ArrayList<>();
        String head = (vo.getPassiveTypeLabel() != null ? "【" + vo.getPassiveTypeLabel() + "】" : "")
                + (StringUtils.hasText(vo.getName()) ? vo.getName() : "未命名被动");
        blocks.add(head);
        if (StringUtils.hasText(vo.getConditionText())) {
            blocks.add(vo.getConditionText());
        }
        if (vo.getEffectTexts() != null && !vo.getEffectTexts().isEmpty()) {
            blocks.add("效果：" + String.join("；", vo.getEffectTexts()));
        } else {
            blocks.add("效果：暂无");
        }
        return String.join("\n", blocks);
    }

    private static String passiveTypeLabel(PassiveSkillType type) {
        return type != null ? type.label() : "被动";
    }

    private static String skillChargeMatchText(SkillCharge charge, Function<String, String> skillNameFn) {
        SkillChargeMatchMode mode = charge.getSkillChargeMatch();
        if (mode == null || mode == SkillChargeMatchMode.ANY) {
            return "任意技能";
        }
        if (mode == SkillChargeMatchMode.ANY_TYPE) {
            return activeSkillTypeLabel(charge.getMatchSkillType()) + "类技能";
        }
        return "技能「" + nameOrId(skillNameFn, charge.getMatchSkillId()) + "」";
    }

    private static String targetLabel(SkillEffectTarget t) {
        if (t == null) {
            return "目标";
        }
        return t.getLabel();
    }

    private static String attrKeyLabel(AttrModifyKey key) {
        return key != null ? key.getLabel() : "属性";
    }

    private static String readTokenLabel(JsonNode n) {
        String roleRaw = text(n, "readRole");
        if (!StringUtils.hasText(roleRaw)) {
            roleRaw = "SELF";
        }
        String role = roleLabel(roleRaw);
        if ("GLOBAL".equals(roleRaw)) {
            return role + "·" + attrReadKeyLabel(text(n, "readKey"), "ELAPSED_ACTION");
        }
        String cat = text(n, "readCategory");
        if (!StringUtils.hasText(cat)) {
            cat = "ATTR";
        }
        if ("ANCHOR".equals(cat)) {
            String metric = "SKILL_DAMAGE".equals(text(n, "anchorMetric")) ? "锚点技能伤害" : "伤害值";
            return role + "·锚点·" + metric;
        }
        if ("DAMAGE".equals(cat)) {
            String side = "RECEIVE".equals(text(n, "damageSide")) ? "受到" : "造成";
            String metric;
            if ("ACTIVE_COUNT".equals(text(n, "damageMetric"))) {
                metric = "主动技能伤害次数";
            } else if ("COUNT".equals(text(n, "damageMetric"))) {
                metric = "累计伤害次数";
            } else {
                metric = "累计伤害量";
            }
            return role + "·伤害·" + side + "·" + metric;
        }
        if ("SKILL".equals(cat)) {
            String match = text(n, "skillMatch");
            String scope;
            if ("ANY_TYPE".equals(match)) {
                scope = "指定类型(" + activeSkillTypeLabel(parseSkillType(text(n, "matchSkillType"))) + ")";
            } else if ("SPECIFIC".equals(match)) {
                scope = "指定技能";
            } else {
                scope = "任意";
            }
            String metric = "RECEIVE".equals(text(n, "skillMetric")) ? "受到次数" : "释放次数";
            return role + "·技能·" + scope + "·" + metric;
        }
        return role + "·" + attrReadKeyLabel(text(n, "readKey"), "ATK");
    }

    private static String roleLabel(String role) {
        try {
            return FormulaReadRole.valueOf(role).getLabel();
        } catch (Exception e) {
            return "自己";
        }
    }

    private static String attrReadKeyLabel(String key, String fallback) {
        String k = StringUtils.hasText(key) ? key : fallback;
        try {
            return FormulaReadKey.valueOf(k).getLabel();
        } catch (Exception e) {
            return k;
        }
    }

    private static ActiveSkillType parseSkillType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return ActiveSkillType.valueOf(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static String opLabel(String op) {
        if (!StringUtils.hasText(op)) {
            return "";
        }
        return switch (op) {
            case "*" -> "×";
            case "/" -> "÷";
            case "+" -> "+";
            case "-" -> "-";
            default -> op;
        };
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) {
            return "";
        }
        return v.asText("");
    }

    private static String nameOrId(Function<String, String> fn, String id) {
        if (!StringUtils.hasText(id)) {
            return "未指定";
        }
        if (fn != null) {
            String n = fn.apply(id);
            if (StringUtils.hasText(n)) {
                return n;
            }
        }
        return id;
    }

    private static String stripTrailingZeros(BigDecimal n) {
        return n.stripTrailingZeros().toPlainString();
    }
}
