package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.mapper.SkillEffectMapper;

import java.util.List;

@Service
public class SkillEffectService extends WxServiceImpl<SkillEffectMapper, SkillEffect> {

    public List<SkillEffect> listBySkillId(String skillId) {
        return this.find().eq(SkillEffect::getSkillId, skillId).orderByAsc(SkillEffect::getSort).list();
    }
}
