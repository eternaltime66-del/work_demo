package org.wx.core.wxBusiness.game.battle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.BattleStartApplyRule;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadKey;
import org.wx.core.wxBusiness.game.entity.enums.FormulaReadRole;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeEvent;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;
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
        return formatOutputLike(
                effect.getEffectType(),
                effect.getTargetType(),
                null,
                effect.getAttrKey(),
                effect.getAttrDir(),
                effect.getFormulaJson(),
                effect.getHitSegments(),
                effect.getTriggerRate(),
                effect.getDurationAv(),
                null
        );
    }

    /** 50 AV = 1 秒 */
    public static String formatAvSec(Integer av) {
        int n = av == null ? 0 : Math.max(0, av);
        double sec = n / 50.0;
        if (sec < 60) {
            if (Math.abs(sec - Math.round(sec)) < 1e-6) {
                return Math.round(sec) + "秒";
            }
            String s = String.format(java.util.Locale.ROOT, "%.2f", sec).replaceAll("\\.?0+$", "");
            return s + "秒";
        }
        int min = (int) Math.floor(sec / 60);
        double rem = sec - min * 60;
        String remText = String.format(java.util.Locale.ROOT, "%.2f", rem).replaceAll("\\.?0+$", "");
        return min + "分 " + remText + "秒";
    }

    /** 非 100% 时前置「N% 」 */
    private static String triggerRatePrefix(Integer triggerRate) {
        if (triggerRate == null || triggerRate >= 100) {
            return "";
        }
        return Math.max(0, triggerRate) + "% ";
    }

    private static String durationAvSuffix(Integer durationAv) {
        if (durationAv == null || durationAv <= 0) {
            return "";
        }
        return "（持续 " + formatAvSec(durationAv) + "）";
    }

    public static String chargeLine(SkillCharge charge, Function<String, String> skillNameFn) {
        if (charge == null) {
            return "";
        }
        int gain = charge.getChargeGain() != null ? charge.getChargeGain() : 0;
        if (charge.getConditionType() == ChargeConditionType.ACTION_VALUE) {
            int every = charge.getEveryActionValue() != null ? charge.getEveryActionValue() : 0;
            return "每 " + formatAvSec(every) + " 充能+" + gain;
        }
        String event = skillChargeEventLabel(charge.getSkillChargeEvent());
        SkillChargeMatchMode mode = charge.getSkillChargeMatch();
        if (mode == SkillChargeMatchMode.ANY_TYPE) {
            return event + " " + activeSkillTypeLabel(charge.getMatchSkillType()) + " 类型 技能 充能+" + gain;
        }
        if (mode == SkillChargeMatchMode.ANY_SCHOOL) {
            return event + " 流派「" + SkillSchoolUnit.normalizeSchool(charge.getMatchSkillSchool()) + "」技能 充能+" + gain;
        }
        if (mode == SkillChargeMatchMode.ANY_ELEMENT) {
            return event + " " + damageElementLabel(charge.getMatchDamageElement()) + " 元素技能 充能+" + gain;
        }
        if (mode == SkillChargeMatchMode.SPECIFIC) {
            return event + " 「" + nameOrId(skillNameFn, charge.getMatchSkillId()) + "」 充能+" + gain;
        }
        return event + " 任意技能 充能+" + gain;
    }

    private static String skillChargeEventLabel(SkillChargeEvent event) {
        if (event == null) {
            return "释放";
        }
        return switch (event) {
            case RECEIVE -> "受到";
            case DEAL_DAMAGE -> "造成伤害以";
            case TAKE_DAMAGE -> "受到伤害以";
            case KILL -> "击杀时以";
            case CAST -> "释放";
        };
    }

    public static String needChargeText(ActiveSkill skill) {
        if (skill == null) {
            return "";
        }
        // 按充能模式文案（普攻默认也是 SELF_BASE_ACTION，不要写成「无需充能」）
        if (skill.getNeedChargeMode() == NeedChargeMode.SELF_BASE_ACTION) {
            return "所需充能 行动值";
        }
        if (skill.getNeedChargeMode() == NeedChargeMode.FORMULA) {
            return "所需充能 公式";
        }
        int n = skill.getNeedCharge() != null ? skill.getNeedCharge() : 0;
        return "所需充能 " + n;
    }

    public static String castLimitText(ActiveSkill skill) {
        if (skill == null) {
            return "";
        }
        Integer max = skill.getMaxCastSkill();
        if (max == null || max <= 0) {
            return "";
        }
        return "最多释放 " + max + " 次";
    }

    public static SkillDescVo describeActiveSkill(
            ActiveSkill skill,
            List<SkillCharge> charges,
            List<SkillEffect> effects,
            Function<String, String> skillNameFn
    ) {
        return describeActiveSkill(skill, charges, effects, null, skillNameFn);
    }

    public static SkillDescVo describeActiveSkill(
            ActiveSkill skill,
            List<SkillCharge> charges,
            List<SkillEffect> effects,
            List<SkillOutput> outputs,
            Function<String, String> skillNameFn
    ) {
        return describeActiveSkill(skill, charges, effects, outputs, skillNameFn, null);
    }

    public static SkillDescVo describeActiveSkill(
            ActiveSkill skill,
            List<SkillCharge> charges,
            List<SkillEffect> effects,
            List<SkillOutput> outputs,
            Function<String, String> skillNameFn,
            Function<String, BuffDef> buffFn
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
        if (outputs != null && !outputs.isEmpty()) {
            for (SkillOutput o : outputs) {
                String line = outputLine(o, buffFn);
                if (StringUtils.hasText(line)) {
                    effectTexts.add(line);
                }
            }
        } else if (effects != null) {
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
        return describePassive(skill, itemNameFn, skillNameFn, null);
    }

    public static PassiveDescVo describePassive(
            PassiveSkill skill,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn,
            Function<String, BuffDef> buffFn
    ) {
        List<PassiveCondition> conditions = skill != null ? skill.getConditions() : null;
        List<PassiveEffect> effects = skill != null ? skill.getEffects() : null;
        return describePassive(skill, conditions, effects, itemNameFn, skillNameFn, buffFn);
    }

    public static PassiveDescVo describePassive(
            PassiveSkill skill,
            List<PassiveCondition> conditions,
            List<PassiveEffect> effects,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn
    ) {
        return describePassive(skill, conditions, effects, itemNameFn, skillNameFn, null);
    }

    public static PassiveDescVo describePassive(
            PassiveSkill skill,
            List<PassiveCondition> conditions,
            List<PassiveEffect> effects,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn,
            Function<String, BuffDef> buffFn
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
        if (skill.getPassiveType() != null && skill.getPassiveType().isBattlePassive()) {
            appendBattleTriggerTexts(effectTexts, skill);
            appendOutputTexts(effectTexts, skill.getOutputs(), buffFn);
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

    private static void appendBattleTriggerTexts(List<String> effectTexts, PassiveSkill skill) {
        if (effectTexts == null || skill == null || skill.getPassiveType() == null) {
            return;
        }
        PassiveSkillType type = skill.getPassiveType();
        if (type == PassiveSkillType.BATTLE_START) {
            BattleStartApplyRule rule = skill.getStartApplyRule() != null
                    ? skill.getStartApplyRule() : BattleStartApplyRule.IMMEDIATE;
            if (rule == BattleStartApplyRule.AT_ELAPSED_ONCE) {
                effectTexts.add("第一次达到 " + formatAvSec(skill.getStartElapsedAv()) + " 时 触发一次");
            } else if (rule == BattleStartApplyRule.EVERY_ELAPSED) {
                effectTexts.add("每 " + formatAvSec(skill.getStartElapsedAv()) + " 脉冲一次");
            } else {
                effectTexts.add("开战立即触发");
            }
        } else if (type == PassiveSkillType.BATTLE_JUDGE || type == PassiveSkillType.BATTLE_PULSE) {
            String left = formatFormula(skill.getLeftFormulaJson());
            String right = formatFormula(skill.getRightFormulaJson());
            boolean judge = type == PassiveSkillType.BATTLE_JUDGE;
            if (judge) {
                String op = skill.getCompareOp() != null ? skill.getCompareOp().symbol() : "?";
                effectTexts.add("判定：当 "
                        + (StringUtils.hasText(left) ? left : "?")
                        + " " + op + " "
                        + (StringUtils.hasText(right) ? right : "?")
                        + " 时生效");
            } else {
                // 脉冲固定「每当达到」（阈值阶梯）
                effectTexts.add("脉冲：每当 "
                        + (StringUtils.hasText(left) ? left : "?")
                        + " 达到 "
                        + (StringUtils.hasText(right) ? right : "?")
                        + " 时触发");
            }
            Integer max = skill.getMaxTriggerPerBattle();
            if (max != null && max > 0) {
                effectTexts.add("本场最多触发 " + max + " 次");
            }
        } else if (type == PassiveSkillType.BATTLE_COMBAT) {
            String event = skill.getCombatEvent() != null ? skill.getCombatEvent().getLabel() : "战斗事件";
            effectTexts.add("事件：" + event);
            Integer max = skill.getMaxTriggerPerBattle();
            if (max != null && max > 0) {
                effectTexts.add("本场最多触发 " + max + " 次");
            }
        }
    }

    private static void appendOutputTexts(
            List<String> effectTexts,
            List<SkillOutput> outputs,
            Function<String, BuffDef> buffFn
    ) {
        if (effectTexts == null || outputs == null) {
            return;
        }
        for (SkillOutput o : outputs) {
            String line = outputLine(o, buffFn);
            if (StringUtils.hasText(line)) {
                effectTexts.add(line);
            }
        }
    }

    public static String outputLine(SkillOutput output) {
        return outputLine(output, null);
    }

    public static String outputLine(SkillOutput output, Function<String, BuffDef> buffFn) {
        if (output == null) {
            return "";
        }
        if (output.getOutputKind() == SkillOutputKind.APPEND_BUFF) {
            return appendBuffLine(output, buffFn);
        }
        SkillEffectType effectType;
        if (output.getOutputKind() == SkillOutputKind.ATTR) {
            effectType = SkillEffectType.ATTR_MODIFY;
        } else {
            effectType = output.getEffectType() != null ? output.getEffectType() : SkillEffectType.DAMAGE;
        }
        return formatOutputLike(
                effectType,
                output.getTargetType(),
                output.getDamageElement(),
                output.getAttrKey(),
                output.getAttrDir(),
                output.getFormulaJson(),
                output.getHitSegments(),
                output.getTriggerRate(),
                output.getDurationAv(),
                output.getOutputKind()
        );
    }

    /** 追加 BUFF：对 目标 追加「名称」：效果摘要（持续） */
    public static String appendBuffLine(SkillOutput output, Function<String, BuffDef> buffFn) {
        if (output == null) {
            return "";
        }
        String rate = triggerRatePrefix(output.getTriggerRate());
        String target = targetLabel(output.getTargetType());
        BuffDef def = null;
        if (buffFn != null && StringUtils.hasText(output.getBuffDefId())) {
            def = buffFn.apply(output.getBuffDefId());
        }
        String name = null;
        if (def != null && StringUtils.hasText(def.getName())) {
            name = def.getName();
        } else if (StringUtils.hasText(output.getBuffDefName())) {
            name = output.getBuffDefName();
        }
        Integer durAv = output.getDurationAv();
        if ((durAv == null || durAv <= 0) && def != null) {
            durAv = def.getDurationAv();
        }
        String dur = durationAvSuffix(durAv);
        String body = buffEffectBody(def);
        StringBuilder sb = new StringBuilder();
        sb.append(rate).append("对 ").append(target).append(" 追加");
        if (StringUtils.hasText(name)) {
            sb.append("「").append(name).append("」");
        } else {
            sb.append("BUFF");
        }
        if (StringUtils.hasText(body)) {
            sb.append("：").append(body);
        }
        sb.append(dur);
        return sb.toString();
    }

    /** BUFF 本体效果（不含挂载目标） */
    public static String buffEffectBody(BuffDef def) {
        if (def == null || def.getBuffKind() == null) {
            return "";
        }
        BuffKind kind = def.getBuffKind();
        if (kind == BuffKind.ATTR) {
            return attrBuffBody(def);
        }
        if (kind == BuffKind.PULSE) {
            return pulseBuffBody(def);
        }
        if (kind == BuffKind.JUDGE_ATTR) {
            String judge = judgePrefix(def);
            String attr = attrBuffBody(def);
            if (StringUtils.hasText(judge) && StringUtils.hasText(attr)) {
                return judge + "时 " + attr;
            }
            return StringUtils.hasText(attr) ? attr : judge;
        }
        if (kind == BuffKind.JUDGE_BURST) {
            String judge = judgePrefix(def);
            String burst = burstBuffBody(def);
            if (StringUtils.hasText(judge) && StringUtils.hasText(burst)) {
                return judge + "时 " + burst;
            }
            return StringUtils.hasText(burst) ? burst : judge;
        }
        if (kind == BuffKind.DODGE) {
            int chance = def.getDodgeChance() == null ? 0 : Math.max(0, def.getDodgeChance());
            String scope = PassiveSkillMatchUnit.matchScopeLabel(
                    def.getSkillMatchMode(),
                    def.getMatchSkillType(),
                    def.getMatchSkillSchool(),
                    def.getMatchDamageElement(),
                    def.getMatchSkillId()
            );
            return "闪避" + chance + "% · " + scope;
        }
        return "";
    }

    private static String judgePrefix(BuffDef def) {
        String left = formatFormula(def.getLeftFormulaJson());
        String right = formatFormula(def.getRightFormulaJson());
        if (!StringUtils.hasText(left) && !StringUtils.hasText(right)) {
            return "";
        }
        String op = def.getCompareOp() != null ? def.getCompareOp().label() : "比较";
        return "当 " + (StringUtils.hasText(left) ? left : "?")
                + " " + op + " " + (StringUtils.hasText(right) ? right : "?");
    }

    private static String attrBuffBody(BuffDef def) {
        String dir = def.getAttrDir() == AttrModifyDirection.DECREASE ? "减少" : "增加";
        String formula = formatFormula(def.getFormulaJson());
        return dir + attrKeyLabel(def.getAttrKey()) + (StringUtils.hasText(formula) ? " " + formula : "");
    }

    private static String pulseBuffBody(BuffDef def) {
        String every = def.getPulseEveryAv() != null && def.getPulseEveryAv() > 0
                ? formatAvSec(def.getPulseEveryAv()) : "—";
        String target = targetLabel(def.getPulseTargetType());
        String formula = formatFormula(def.getFormulaJson());
        String formulaPart = StringUtils.hasText(formula) ? " " + formula : "";
        if (def.getPulseEffectType() == SkillEffectType.HEAL) {
            return "每 " + every + " 对 " + target + " 治疗" + formulaPart;
        }
        String el = damageElementLabel(def.getDamageElement());
        return "每 " + every + " 对 " + target + " 造成 " + el + "伤害" + formulaPart;
    }

    private static String burstBuffBody(BuffDef def) {
        String formula = formatFormula(def.getFormulaJson());
        String formulaPart = StringUtils.hasText(formula) ? " " + formula : "";
        if (def.getPulseEffectType() == SkillEffectType.HEAL) {
            return "对 持有者 治疗" + formulaPart;
        }
        if (def.getPulseEffectType() == SkillEffectType.ATTR_MODIFY) {
            return attrBuffBody(def);
        }
        String el = damageElementLabel(def.getDamageElement());
        return "对 持有者 造成 " + el + "伤害" + formulaPart;
    }

    /**
     * 新版效果摘要：对 目标 造成 物理伤害 自己·攻击
     */
    private static String formatOutputLike(
            SkillEffectType effectType,
            SkillEffectTarget targetType,
            DamageElement damageElement,
            AttrModifyKey attrKey,
            AttrModifyDirection attrDir,
            String formulaJson,
            Integer hitSegments,
            Integer triggerRate,
            Integer durationAv,
            SkillOutputKind outputKind
    ) {
        String rate = triggerRatePrefix(triggerRate);
        String target = targetLabel(targetType);
        String formula = formatFormula(formulaJson);
        String formulaPart = StringUtils.hasText(formula) ? " " + formula : "";
        String seg = "";
        if (hitSegments != null && hitSegments > 1) {
            seg = " × " + hitSegments;
        }
        String dur = durationAvSuffix(durationAv);
        if (outputKind == SkillOutputKind.APPEND_BUFF) {
            // 完整文案走 appendBuffLine；此处仅兜底
            return rate + "对 " + target + " 追加BUFF" + dur;
        }
        if (outputKind == SkillOutputKind.ATTR || effectType == SkillEffectType.ATTR_MODIFY) {
            String dir = attrDir == AttrModifyDirection.DECREASE ? "减少" : "增加";
            return rate + "对 " + target + " " + dir + attrKeyLabel(attrKey) + formulaPart + dur;
        }
        if (effectType == SkillEffectType.HEAL) {
            return rate + "对 " + target + " 治疗" + formulaPart + seg;
        }
        String el = damageElementLabel(damageElement);
        return rate + "对 " + target + " 造成 " + el + "伤害" + formulaPart + seg;
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
        String type = StringUtils.hasText(vo.getSkillTypeLabel()) ? vo.getSkillTypeLabel() : "技能";
        String need = StringUtils.hasText(vo.getNeedChargeText()) ? vo.getNeedChargeText() : "";
        if (StringUtils.hasText(need)) {
            blocks.add(type + " · " + need);
        } else {
            blocks.add(type);
        }
        if (StringUtils.hasText(vo.getCastLimitText())) {
            blocks.add(vo.getCastLimitText());
        }
        if (vo.getChargeTexts() != null) {
            for (String c : vo.getChargeTexts()) {
                if (StringUtils.hasText(c)) {
                    blocks.add(c);
                }
            }
        }
        if (vo.getEffectTexts() != null) {
            for (String e : vo.getEffectTexts()) {
                if (StringUtils.hasText(e)) {
                    blocks.add(e);
                }
            }
        }
        return String.join("\n", blocks);
    }

    private static String joinPassiveSummary(PassiveDescVo vo) {
        List<String> blocks = new ArrayList<>();
        String type = StringUtils.hasText(vo.getPassiveTypeLabel()) ? vo.getPassiveTypeLabel() : "被动";
        String cond = StringUtils.hasText(vo.getConditionText()) ? vo.getConditionText() : "";
        // 「始终生效」不写进摘要，避免与触发时机叠在一起产生歧义
        if (StringUtils.hasText(cond) && !"始终生效".equals(cond)) {
            blocks.add(type + " · " + cond);
        } else {
            blocks.add(type);
        }
        if (vo.getEffectTexts() != null) {
            for (String e : vo.getEffectTexts()) {
                if (StringUtils.hasText(e)) {
                    blocks.add(e);
                }
            }
        }
        return String.join("\n", blocks);
    }

    private static String passiveTypeLabel(PassiveSkillType type) {
        return type != null ? type.shortLabel() : "被动";
    }

    private static String damageElementLabel(DamageElement el) {
        DamageElement e = SkillSchoolUnit.normalizeElement(el);
        return e.getLabel();
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
            } else if ("ANY_SCHOOL".equals(match)) {
                scope = "指定流派(" + SkillSchoolUnit.normalizeSchool(text(n, "matchSkillSchool")) + ")";
            } else if ("ANY_ELEMENT".equals(match)) {
                scope = "指定元素(" + damageElementLabel(parseDamageElement(text(n, "matchDamageElement"))) + ")";
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

    private static DamageElement parseDamageElement(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DamageElement.PHYSICAL;
        }
        try {
            return DamageElement.valueOf(raw);
        } catch (Exception e) {
            return DamageElement.PHYSICAL;
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
