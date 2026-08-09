package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;

/**
 * SkillOutput 展示文案：按效果自动生成，无需手填名称。
 */
public final class SkillOutputDescUnit {

    private SkillOutputDescUnit() {
    }

    public static String autoName(SkillOutput out) {
        if (out == null || out.getOutputKind() == null) {
            return "输出";
        }
        String target = targetLabel(out.getTargetType());
        return switch (out.getOutputKind()) {
            case ATTR -> {
                String dir = out.getAttrDir() == AttrModifyDirection.DECREASE ? "减少" : "增加";
                String attr = out.getAttrKey() != null ? out.getAttrKey().name() : "属性";
                String dur = out.getDurationAv() != null && out.getDurationAv() > 0
                        ? "（持续" + out.getDurationAv() + "行动值）" : "";
                yield "对" + target + dir + attr + dur;
            }
            case EFFECT -> {
                if (out.getEffectType() == SkillEffectType.HEAL) {
                    yield "对" + target + "治疗";
                }
                String el = out.getDamageElement() != null && out.getDamageElement() != DamageElement.PHYSICAL
                        ? "（" + out.getDamageElement().name() + "）" : "";
                yield "对" + target + "造成伤害" + el;
            }
            case APPEND_BUFF -> {
                if (out.getBuffDefName() != null && !out.getBuffDefName().isBlank()) {
                    yield "对" + target + "追加「" + out.getBuffDefName() + "」";
                }
                yield "对" + target + "追加BUFF";
            }
        };
    }

    /** 战斗/日志展示：始终按效果自动生成 */
    public static String display(SkillOutput out) {
        return autoName(out);
    }

    private static String targetLabel(SkillEffectTarget t) {
        if (t == null) {
            return "目标";
        }
        return t.getLabel() != null ? t.getLabel() : t.name();
    }
}
