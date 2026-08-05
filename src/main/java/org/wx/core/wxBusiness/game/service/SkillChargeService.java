package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.mapper.SkillChargeMapper;

import java.util.List;

@Service
public class SkillChargeService extends WxServiceImpl<SkillChargeMapper, SkillCharge> {

    public List<SkillCharge> listBySkillId(String skillId) {
        return this.find().eq(SkillCharge::getSkillId, skillId).orderByAsc(SkillCharge::getSort).list();
    }
}
