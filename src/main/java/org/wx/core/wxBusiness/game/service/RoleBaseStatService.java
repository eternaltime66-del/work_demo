package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.battle.AtkSpeedCalcUnit;
import org.wx.core.wxBusiness.game.entity.RoleBaseStat;
import org.wx.core.wxBusiness.game.entity.enums.PlayerRoleCategory;
import org.wx.core.wxBusiness.game.mapper.RoleBaseStatMapper;

import java.math.BigDecimal;

@Service
public class RoleBaseStatService extends WxServiceImpl<RoleBaseStatMapper, RoleBaseStat> {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** 比例单位 1%；造成/受到伤害默认 100 */
    public void prepareRatios(RoleBaseStat entity) {
        if (entity == null) {
            return;
        }
        if (entity.getRoleCategory() == null) {
            entity.setRoleCategory(Boolean.TRUE.equals(entity.getMainRole())
                    ? PlayerRoleCategory.HERO
                    : PlayerRoleCategory.PARTNER);
        }
        if (entity.getRoleCategory() == PlayerRoleCategory.HERO) {
            entity.setMainRole(true);
        } else if (entity.getMainRole() == null) {
            entity.setMainRole(false);
        }
        if (entity.getRoleCategory() == PlayerRoleCategory.SUMMON) {
            if (entity.getInheritAtkRatio() == null) {
                entity.setInheritAtkRatio(HUNDRED);
            }
            if (entity.getInheritDefRatio() == null) {
                entity.setInheritDefRatio(HUNDRED);
            }
            if (entity.getInheritHpRatio() == null) {
                entity.setInheritHpRatio(HUNDRED);
            }
            // 召唤物基础三维由继承比例决定，绝对基础值置 0
            if (entity.getBaseAtk() == null) {
                entity.setBaseAtk(0);
            }
            if (entity.getBaseHp() == null) {
                entity.setBaseHp(0);
            }
            if (entity.getBaseDef() == null) {
                entity.setBaseDef(0);
            }
        }
        if (entity.getDealDmgRatio() == null) {
            entity.setDealDmgRatio(HUNDRED);
        }
        if (entity.getTakenDmgRatio() == null) {
            entity.setTakenDmgRatio(HUNDRED);
        }
        if (entity.getDealElementDmgRatio() == null) {
            entity.setDealElementDmgRatio(HUNDRED);
        }
        if (entity.getTakenElementDmgRatio() == null) {
            entity.setTakenElementDmgRatio(HUNDRED);
        }
        if (entity.getDealPhysDmgRatio() == null) {
            entity.setDealPhysDmgRatio(HUNDRED);
        }
        if (entity.getTakenPhysDmgRatio() == null) {
            entity.setTakenPhysDmgRatio(HUNDRED);
        }
        if (entity.getLifeStealRatio() == null) {
            entity.setLifeStealRatio(BigDecimal.ZERO);
        }
        if (entity.getFinalAtkRatio() == null) {
            entity.setFinalAtkRatio(HUNDRED);
        }
        if (entity.getFinalHpRatio() == null) {
            entity.setFinalHpRatio(HUNDRED);
        }
        if (entity.getFinalDefRatio() == null) {
            entity.setFinalDefRatio(HUNDRED);
        }
        if (entity.getAtkSpeedUpRatio() == null) {
            entity.setAtkSpeedUpRatio(BigDecimal.ZERO);
        }
        if (entity.getAtkSpeedDownRatio() == null) {
            entity.setAtkSpeedDownRatio(BigDecimal.ZERO);
        }
        syncActionFromAtkSpeed(entity);
    }

    /** 以攻速为准反推行动值；缺攻速时用行动值反推攻速，默认攻速 1 */
    public void syncActionFromAtkSpeed(RoleBaseStat entity) {
        if (entity == null) {
            return;
        }
        if (entity.getBaseAtkSpeed() != null && entity.getBaseAtkSpeed().compareTo(BigDecimal.ZERO) > 0) {
            entity.setBaseAtkSpeed(AtkSpeedCalcUnit.normalizeAtkSpeedBd(entity.getBaseAtkSpeed()));
            entity.setBaseAction(AtkSpeedCalcUnit.actionFromAtkSpeed(entity.getBaseAtkSpeed()));
            return;
        }
        if (entity.getBaseAction() != null && entity.getBaseAction() > 0) {
            entity.setBaseAtkSpeed(AtkSpeedCalcUnit.atkSpeedFromAction(entity.getBaseAction()));
            return;
        }
        entity.setBaseAtkSpeed(BigDecimal.ONE);
        entity.setBaseAction(AtkSpeedCalcUnit.actionFromAtkSpeed(BigDecimal.ONE));
    }

    public void savePrepared(RoleBaseStat entity) {
        prepareRatios(entity);
        this.saveOrUpdate(entity);
    }
}
