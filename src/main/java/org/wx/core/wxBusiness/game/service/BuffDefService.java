package org.wx.core.wxBusiness.game.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.enums.BuffKind;
import org.wx.core.wxBusiness.game.entity.enums.BuffStackMode;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.mapper.BuffDefMapper;

import java.util.List;

@Service
public class BuffDefService extends WxServiceImpl<BuffDefMapper, BuffDef> {

    public List<BuffDef> listEnabledOptions() {
        return this.find()
                .eq(BuffDef::getEnable, true)
                .orderByAsc(BuffDef::getSort)
                .orderByAsc(BuffDef::getName)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveValidated(BuffDef entity) {
        ErrorFactory.notNull(entity, "参数不能为空");
        ErrorFactory.notEmpty(entity.getName(), "名称不能为空");
        ErrorFactory.notNull(entity.getBuffKind(), "请选择 BUFF 类型");
        if (entity.getBeneficial() == null) {
            entity.setBeneficial(true);
        }
        if (entity.getDispelable() == null) {
            entity.setDispelable(true);
        }
        if (entity.getStackMode() == null) {
            entity.setStackMode(BuffStackMode.NONE);
        }
        if (entity.getEnable() == null) {
            entity.setEnable(true);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getDurationAv() == null) {
            entity.setDurationAv(0);
        }
        if (entity.getMaxStacks() == null) {
            entity.setMaxStacks(0);
        }
        validateByKind(entity);
        this.saveOrUpdate(entity);
    }

    private void validateByKind(BuffDef entity) {
        BuffKind kind = entity.getBuffKind();
        if (kind == BuffKind.ATTR || kind == BuffKind.JUDGE_ATTR) {
            ErrorFactory.notNull(entity.getAttrKey(), "请选择属性");
            ErrorFactory.notNull(entity.getAttrDir(), "请选择增减");
            ErrorFactory.notEmpty(entity.getFormulaJson(), "请配置属性公式");
        }
        if (kind == BuffKind.PULSE) {
            ErrorFactory.throwError(entity.getPulseEveryAv() == null || entity.getPulseEveryAv() <= 0,
                    "请填写脉冲间隔行动值");
            if (entity.getPulseEffectType() == null) {
                entity.setPulseEffectType(SkillEffectType.DAMAGE);
            }
            ErrorFactory.notEmpty(entity.getFormulaJson(), "请配置脉冲公式");
        }
        if (kind == BuffKind.JUDGE_ATTR || kind == BuffKind.JUDGE_BURST) {
            ErrorFactory.notEmpty(entity.getLeftFormulaJson(), "请配置左公式");
            ErrorFactory.notNull(entity.getCompareOp(), "请选择比较符");
            ErrorFactory.notEmpty(entity.getRightFormulaJson(), "请配置右公式");
        }
        if (kind == BuffKind.JUDGE_BURST) {
            if (entity.getPulseEffectType() == null) {
                entity.setPulseEffectType(SkillEffectType.DAMAGE);
            }
            if (entity.getPulseEffectType() == SkillEffectType.ATTR_MODIFY) {
                ErrorFactory.notNull(entity.getAttrKey(), "请选择爆发属性");
                ErrorFactory.notNull(entity.getAttrDir(), "请选择增减");
            }
            ErrorFactory.notEmpty(entity.getFormulaJson(), "请配置爆发公式");
        }
        if (kind == BuffKind.DODGE) {
            ErrorFactory.throwError(entity.getDodgeChance() == null || entity.getDodgeChance() <= 0,
                    "请填写闪避概率（1~100）");
            ErrorFactory.throwError(entity.getDodgeChance() > 100, "闪避概率不能超过100");
            if (entity.getSkillMatchMode() == null) {
                entity.setSkillMatchMode(SkillChargeMatchMode.ANY);
            }
            normalizeDodgeMatch(entity);
        }
    }

    private void normalizeDodgeMatch(BuffDef e) {
        SkillChargeMatchMode mode = e.getSkillMatchMode();
        if (mode == SkillChargeMatchMode.ANY) {
            e.setMatchSkillType(null);
            e.setMatchSkillSchool(null);
            e.setMatchDamageElement(null);
            e.setMatchSkillId(null);
            return;
        }
        if (mode == SkillChargeMatchMode.ANY_TYPE) {
            ErrorFactory.notNull(e.getMatchSkillType(), "请选择闪避匹配的技能类型");
            e.setMatchSkillSchool(null);
            e.setMatchDamageElement(null);
            e.setMatchSkillId(null);
            return;
        }
        if (mode == SkillChargeMatchMode.ANY_SCHOOL) {
            ErrorFactory.notEmpty(e.getMatchSkillSchool(), "请填写闪避匹配的流派");
            e.setMatchSkillSchool(e.getMatchSkillSchool().trim());
            e.setMatchSkillType(null);
            e.setMatchDamageElement(null);
            e.setMatchSkillId(null);
            return;
        }
        if (mode == SkillChargeMatchMode.ANY_ELEMENT) {
            if (e.getMatchDamageElement() == null) {
                e.setMatchDamageElement(DamageElement.PHYSICAL);
            }
            e.setMatchSkillType(null);
            e.setMatchSkillSchool(null);
            e.setMatchSkillId(null);
            return;
        }
        if (mode == SkillChargeMatchMode.SPECIFIC) {
            ErrorFactory.notEmpty(e.getMatchSkillId(), "请选择闪避匹配的技能");
            e.setMatchSkillType(null);
            e.setMatchSkillSchool(null);
            e.setMatchDamageElement(null);
        }
    }
}
