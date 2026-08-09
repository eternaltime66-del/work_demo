package org.wx.core.wxBusiness.game.battle;

import lombok.Data;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountDirection;
import org.wx.core.wxBusiness.game.battle.enums.SkillCountScope;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

import java.util.Objects;

/**
 * 充能技能次数统计维度：角色 × 方向 × 范围（指定技能/类型/流派/元素/任意）
 */
@Data
public class BattleSkillCountKey {

    private String roleId;
    private SkillCountDirection direction;
    private SkillCountScope scope;
    /** scope=SPECIFIC_SKILL 时有效 */
    private String skillId;
    /** scope=SKILL_TYPE 时有效 */
    private ActiveSkillType skillType;
    /** scope=SKILL_SCHOOL 时有效（已归一） */
    private String skillSchool;
    /** scope=SKILL_ELEMENT 时有效 */
    private DamageElement damageElement;

    public static BattleSkillCountKey ofAny(String roleId, SkillCountDirection direction) {
        BattleSkillCountKey key = new BattleSkillCountKey();
        key.roleId = roleId;
        key.direction = direction;
        key.scope = SkillCountScope.ANY;
        return key;
    }

    public static BattleSkillCountKey ofSkill(String roleId, SkillCountDirection direction, String skillId) {
        BattleSkillCountKey key = new BattleSkillCountKey();
        key.roleId = roleId;
        key.direction = direction;
        key.scope = SkillCountScope.SPECIFIC_SKILL;
        key.skillId = skillId;
        return key;
    }

    public static BattleSkillCountKey ofType(String roleId, SkillCountDirection direction, ActiveSkillType skillType) {
        BattleSkillCountKey key = ofAny(roleId, direction);
        key.scope = SkillCountScope.SKILL_TYPE;
        key.skillType = skillType;
        return key;
    }

    public static BattleSkillCountKey ofSchool(String roleId, SkillCountDirection direction, String skillSchool) {
        BattleSkillCountKey key = ofAny(roleId, direction);
        key.scope = SkillCountScope.SKILL_SCHOOL;
        key.skillSchool = SkillSchoolUnit.normalizeSchool(skillSchool);
        return key;
    }

    public static BattleSkillCountKey ofElement(String roleId, SkillCountDirection direction, DamageElement element) {
        BattleSkillCountKey key = ofAny(roleId, direction);
        key.scope = SkillCountScope.SKILL_ELEMENT;
        key.damageElement = SkillSchoolUnit.normalizeElement(element);
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BattleSkillCountKey that)) return false;
        return Objects.equals(roleId, that.roleId)
                && direction == that.direction
                && scope == that.scope
                && Objects.equals(skillId, that.skillId)
                && skillType == that.skillType
                && Objects.equals(skillSchool, that.skillSchool)
                && damageElement == that.damageElement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, direction, scope, skillId, skillType, skillSchool, damageElement);
    }
}
