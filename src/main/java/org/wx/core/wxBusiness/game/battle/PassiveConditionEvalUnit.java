package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 被动生效条件判定（装备类开战预筛 / 公式类触发时判定）
 */
public final class PassiveConditionEvalUnit {

    private PassiveConditionEvalUnit() {
    }

    public static boolean matchEquipConditions(
            PassiveSkill skill,
            Set<String> equippedItemIds,
            Set<String> equippedSkillIds,
            Collection<org.wx.core.wxBusiness.game.entity.enums.ItemType> equippedItemTypes,
            Collection<org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType> equippedSkillTypes
    ) {
        if (skill == null) {
            return false;
        }
        if (skill.getConditionMode() == null || skill.getConditionMode() == PassiveConditionMode.UNLIMITED) {
            return true;
        }
        List<PassiveCondition> conditions = skill.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }
        for (PassiveCondition c : conditions) {
            if (c == null || c.getConditionType() == null) {
                return false;
            }
            PassiveConditionType type = c.getConditionType();
            if (type == PassiveConditionType.FORMULA_COMPARE) {
                continue; // 触发时再判
            }
            boolean ok = switch (type) {
                case EQUIP_ITEM -> equippedItemIds != null && equippedItemIds.contains(c.getRefItemId());
                case EQUIP_ITEM_TYPE -> equippedItemTypes != null && equippedItemTypes.contains(c.getRefItemType());
                case EQUIP_SKILL -> equippedSkillIds != null && equippedSkillIds.contains(c.getRefSkillId());
                case EQUIP_SKILL_TYPE -> equippedSkillTypes != null && equippedSkillTypes.contains(c.getRefSkillType());
                default -> false;
            };
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchFormulaConditions(
            PassiveSkill skill,
            BattleRuntimeUnit self,
            BattleStatBoard board,
            AnchorEvalContext anchor
    ) {
        if (skill == null) {
            return false;
        }
        if (skill.getConditionMode() == null || skill.getConditionMode() == PassiveConditionMode.UNLIMITED) {
            return true;
        }
        List<PassiveCondition> conditions = skill.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }
        for (PassiveCondition c : conditions) {
            if (c == null || c.getConditionType() != PassiveConditionType.FORMULA_COMPARE) {
                continue;
            }
            double left = FormulaEvalUnit.eval(c.getLeftFormulaJson(), self, self, board, anchor);
            double right = FormulaEvalUnit.eval(c.getRightFormulaJson(), self, self, board, anchor);
            if (!compare(c.getCompareOp(), left, right)) {
                return false;
            }
        }
        return true;
    }

    private static boolean compare(CompareOp op, double left, double right) {
        if (op == null) {
            return false;
        }
        return switch (op) {
            case GT -> left > right;
            case GTE -> left >= right;
            case LT -> left < right;
            case LTE -> left <= right;
            case EQ -> Math.abs(left - right) < 1e-6;
        };
    }
}
