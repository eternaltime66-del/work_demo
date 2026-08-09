package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeConditionType;
import org.wx.core.wxBusiness.game.entity.enums.ChargeScope;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;
import org.wx.core.wxBusiness.game.battle.SkillSchoolUnit;
import org.wx.core.wxBusiness.game.mapper.ActiveSkillMapper;

import java.util.List;

@Service
public class ActiveSkillService extends WxServiceImpl<ActiveSkillMapper, ActiveSkill> {

    @Override
    public boolean save(ActiveSkill entity) {
        normalizeMeta(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(ActiveSkill entity) {
        normalizeMeta(entity);
        return super.updateById(entity);
    }

    @Override
    public boolean saveOrUpdate(ActiveSkill entity) {
        normalizeMeta(entity);
        return super.saveOrUpdate(entity);
    }

    private void normalizeMeta(ActiveSkill entity) {
        if (entity == null) {
            return;
        }
        entity.setSkillSchool(SkillSchoolUnit.normalizeSchool(entity.getSkillSchool()));
        entity.setDamageElement(SkillSchoolUnit.normalizeElement(entity.getDamageElement()));
    }

    public static final String DEFAULT_NORMAL_CODE = "DEFAULT_NORMAL";

    @Resource
    private SkillChargeService skillChargeService;
    @Resource
    private SkillEffectService skillEffectService;
    @Resource
    private SkillOutputService skillOutputService;

    /**
     * 删除技能并级联删除充能 / 旧效果 / V2 输出
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeWithChildren(String skillId) {
        if (Wx.isEmpty(skillId)) {
            return;
        }
        skillChargeService.remove(skillChargeService.find().eq(SkillCharge::getSkillId, skillId).wrapper());
        skillEffectService.remove(skillEffectService.find().eq(SkillEffect::getSkillId, skillId).wrapper());
        skillOutputService.removeBySkillId(skillId);
        this.removeById(skillId);
    }

    /**
     * 确保存在默认普攻：
     * 所需充能=自己基础行动值；充能条件=每 1 行动值 +1；伤害=自己攻击×1，首目标。
     */
    @Transactional(rollbackFor = Exception.class)
    public ActiveSkill ensureDefaultNormalSkill() {
        syncAllNormalChargeDefaults();
        ActiveSkill exist = this.find().eq(ActiveSkill::getCode, DEFAULT_NORMAL_CODE).one();
        if (exist != null) {
            ensureDefaultNormalOutput(exist.getId());
            return exist;
        }
        ActiveSkill skill = new ActiveSkill();
        skill.setName("普攻");
        skill.setCode(DEFAULT_NORMAL_CODE);
        skill.setSkillType(ActiveSkillType.NORMAL);
        skill.setSkillSchool(SkillSchoolUnit.DEFAULT_SCHOOL);
        skill.setDamageElement(DamageElement.PHYSICAL);
        skill.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
        skill.setNeedCharge(0);
        skill.setMaxCastSkill(0);
        skill.setMaxCastGlobal(0);
        skill.setMaxCastAllMeans(0);
        skill.setMaxCastRole(0);
        skill.setSort(0);
        skill.setEnable(true);
        skill.setRemark("系统默认普攻：所需=自身行动值；每1行动值+1充能");
        this.save(skill);

        ensureActionValueChargeEveryOne(skill.getId());
        ensureDefaultNormalOutput(skill.getId());
        return skill;
    }

    private void ensureDefaultNormalOutput(String skillId) {
        if (Wx.isEmpty(skillId)) {
            return;
        }
        List<SkillOutput> outs = skillOutputService.listBySkillId(skillId);
        if (outs != null && !outs.isEmpty()) {
            return;
        }
        String formula = "[{\"kind\":\"PARAM\",\"paramMode\":\"READ\",\"readRole\":\"SELF\",\"readKey\":\"ATK\"}"
                + ",{\"kind\":\"OP\",\"op\":\"*\"}"
                + ",{\"kind\":\"PARAM\",\"paramMode\":\"LITERAL\",\"value\":\"1\"}]";
        SkillOutput out = new SkillOutput();
        out.setSkillId(skillId);
        out.setName("普攻伤害");
        out.setOutputKind(SkillOutputKind.EFFECT);
        out.setTargetType(SkillEffectTarget.FIRST);
        out.setEffectType(SkillEffectType.DAMAGE);
        out.setDamageElement(DamageElement.PHYSICAL);
        out.setHitSegments(1);
        out.setTriggerRate(100);
        out.setDurationAv(0);
        out.setSort(0);
        out.setFormulaJson(formula);
        skillOutputService.save(out);

        // 兼容旧战斗路径：若无 SkillEffect 也补一条
        List<SkillEffect> effects = skillEffectService.listBySkillId(skillId);
        if (effects == null || effects.isEmpty()) {
            SkillEffect effect = new SkillEffect();
            effect.setSkillId(skillId);
            effect.setName("普攻伤害");
            effect.setTargetType(SkillEffectTarget.FIRST);
            effect.setEffectType(SkillEffectType.DAMAGE);
            effect.setHitSegments(1);
            effect.setTriggerRate(100);
            effect.setSort(0);
            effect.setFormulaJson(formula);
            skillEffectService.save(effect);
        }
    }

    /**
     * 将全部普攻统一为：所需=自己基础行动值 + 每1行动值+1充能（已有同条件则不重复添加）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncAllNormalChargeDefaults() {
        List<ActiveSkill> normals = this.find().eq(ActiveSkill::getSkillType, ActiveSkillType.NORMAL).list();
        if (normals == null) {
            return;
        }
        for (ActiveSkill skill : normals) {
            applyNormalChargeDefaults(skill);
        }
    }

    private void applyNormalChargeDefaults(ActiveSkill skill) {
        if (skill == null || Wx.isEmpty(skill.getId())) {
            return;
        }
        boolean dirty = false;
        if (skill.getNeedChargeMode() != NeedChargeMode.SELF_BASE_ACTION) {
            skill.setNeedChargeMode(NeedChargeMode.SELF_BASE_ACTION);
            dirty = true;
        }
        if (skill.getNeedCharge() == null) {
            skill.setNeedCharge(0);
            dirty = true;
        }
        if (dirty) {
            this.updateById(skill);
        }
        ensureActionValueChargeEveryOne(skill.getId());
    }

    /** 保证存在「每经过 1 行动值 +1 充能」条件 */
    private void ensureActionValueChargeEveryOne(String skillId) {
        List<SkillCharge> charges = skillChargeService.listBySkillId(skillId);
        boolean has = charges != null && charges.stream().anyMatch(c ->
                c != null
                        && c.getConditionType() == ChargeConditionType.ACTION_VALUE
                        && c.getEveryActionValue() != null && c.getEveryActionValue() == 1
                        && c.getChargeGain() != null && c.getChargeGain() == 1);
        if (has) {
            return;
        }
        SkillCharge charge = new SkillCharge();
        charge.setSkillId(skillId);
        charge.setName("行动充能");
        charge.setConditionType(ChargeConditionType.ACTION_VALUE);
        charge.setScope(ChargeScope.GLOBAL);
        charge.setEveryActionValue(1);
        charge.setChargeGain(1);
        charge.setSort(0);
        charge.setRemark("普攻默认：每1行动值+1充能");
        skillChargeService.save(charge);
    }
}
