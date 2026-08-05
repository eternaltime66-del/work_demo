package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.mapper.PassiveConditionMapper;

import java.util.Collections;
import java.util.List;

@Service
public class PassiveConditionService extends WxServiceImpl<PassiveConditionMapper, PassiveCondition> {

    public List<PassiveCondition> listBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return Collections.emptyList();
        }
        return this.find()
                .eq(PassiveCondition::getSkillId, skillId)
                .orderByAsc(PassiveCondition::getSort)
                .list();
    }

    public void removeBySkillId(String skillId) {
        if (!StringUtils.hasText(skillId)) {
            return;
        }
        this.remove(new LambdaQueryWrapper<PassiveCondition>()
                .eq(PassiveCondition::getSkillId, skillId));
    }
}
