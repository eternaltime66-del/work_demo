package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.mapper.SkillChargeMapper;

import java.util.List;
import java.util.function.Function;

@Service
public class SkillChargeService extends WxServiceImpl<SkillChargeMapper, SkillCharge> {

    public List<SkillCharge> listBySkillId(String skillId) {
        return this.find().eq(SkillCharge::getSkillId, skillId).orderByAsc(SkillCharge::getSort).list();
    }

    /** 填充指定技能名称（由调用方提供查名函数，避免与 ActiveSkillService 循环依赖） */
    public void fillMatchSkillName(List<SkillCharge> list, Function<String, ActiveSkill> skillFn) {
        if (list == null || list.isEmpty() || skillFn == null) {
            return;
        }
        for (SkillCharge c : list) {
            if (c == null || c.getSkillChargeMatch() != SkillChargeMatchMode.SPECIFIC) {
                continue;
            }
            if (Wx.isEmpty(c.getMatchSkillId())) {
                continue;
            }
            ActiveSkill sk = skillFn.apply(c.getMatchSkillId());
            if (sk != null && !Wx.isEmpty(sk.getName())) {
                c.setMatchSkillName(sk.getName());
            }
        }
    }
}
