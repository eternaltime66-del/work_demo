package org.wx.core.wxBusiness.game.battle;

import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;

/**
 * 锚点被动：触发技能是否匹配配置
 */
public final class PassiveSkillMatchUnit {

    private PassiveSkillMatchUnit() {
    }

    /**
     * 充能技能 = 全部主动技能（普攻 / 大招 / 小技能）。
     * 装备「充能技能槽」仅挂大招/小技能，与此判定无关。
     */
    public static boolean isChargeSkill(ActiveSkill skill) {
        return skill != null && skill.getSkillType() != null;
    }

    public static boolean matchTriggerSkill(PassiveSkill passive, ActiveSkill trigger) {
        if (passive == null || !isChargeSkill(trigger)) {
            return false;
        }
        if (passive.getAnchorType() == null || !passive.getAnchorType().needsSkillMatch()) {
            return true;
        }
        SkillChargeMatchMode mode = passive.getSkillMatchMode();
        if (mode == null || mode == SkillChargeMatchMode.ANY) {
            return true;
        }
        if (mode == SkillChargeMatchMode.ANY_TYPE) {
            return trigger.getSkillType() == passive.getRefSkillType();
        }
        if (mode == SkillChargeMatchMode.SPECIFIC) {
            return trigger.getId() != null && trigger.getId().equals(passive.getRefSkillId());
        }
        return false;
    }
}
