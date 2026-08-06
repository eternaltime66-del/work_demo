package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.entity.enums.MonsterRarity;
import org.wx.core.wxBusiness.game.mapper.MonsterMapper;

@Service
public class MonsterService extends WxServiceImpl<MonsterMapper, Monster> {

    public Monster prepare(Monster entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getRarity() == null) {
            entity.setRarity(MonsterRarity.NORMAL);
        }
        entity.applyRaritySize();
        if (entity.getBaseAtk() == null) {
            entity.setBaseAtk(0);
        }
        if (entity.getBaseHp() == null) {
            entity.setBaseHp(0);
        }
        if (entity.getBaseDef() == null) {
            entity.setBaseDef(0);
        }
        if (entity.getBaseAction() == null) {
            entity.setBaseAction(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        entity.setNormalSkillId(blankToNull(entity.getNormalSkillId()));
        entity.setSmallSkillId(blankToNull(entity.getSmallSkillId()));
        entity.setUltimateSkillId(blankToNull(entity.getUltimateSkillId()));
        return entity;
    }

    private static String blankToNull(String v) {
        return Wx.isEmpty(v) ? null : v.trim();
    }

    public void savePrepared(Monster entity) {
        prepare(entity);
        this.saveOrUpdate(entity);
    }
}
