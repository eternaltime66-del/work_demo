package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.mapper.PassiveEffectMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class PassiveEffectService extends WxServiceImpl<PassiveEffectMapper, PassiveEffect> {

    public List<PassiveEffect> listBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return Collections.emptyList();
        }
        return this.find()
                .eq(PassiveEffect::getSkillId, skillId)
                .orderByAsc(PassiveEffect::getSort)
                .list();
    }

    public List<PassiveEffect> listBySkillIds(Collection<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.find()
                .in(PassiveEffect::getSkillId, skillIds)
                .orderByAsc(PassiveEffect::getSort)
                .list();
    }

    public void removeBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return;
        }
        this.remove(new LambdaQueryWrapper<PassiveEffect>()
                .eq(PassiveEffect::getSkillId, skillId));
    }
}
