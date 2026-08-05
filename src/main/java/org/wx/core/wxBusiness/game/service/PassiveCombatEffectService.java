package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.PassiveCombatEffect;
import org.wx.core.wxBusiness.game.mapper.PassiveCombatEffectMapper;

import java.util.Collection;
import java.util.List;

@Service
public class PassiveCombatEffectService extends WxServiceImpl<PassiveCombatEffectMapper, PassiveCombatEffect> {

    public List<PassiveCombatEffect> listBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return List.of();
        }
        return this.find()
                .eq(PassiveCombatEffect::getSkillId, skillId)
                .orderByAsc(PassiveCombatEffect::getSort)
                .list();
    }

    public List<PassiveCombatEffect> listBySkillIds(Collection<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        return this.find()
                .in(PassiveCombatEffect::getSkillId, skillIds)
                .orderByAsc(PassiveCombatEffect::getSort)
                .list();
    }

    public void removeBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return;
        }
        this.remove(new LambdaQueryWrapper<PassiveCombatEffect>()
                .eq(PassiveCombatEffect::getSkillId, skillId));
    }
}
