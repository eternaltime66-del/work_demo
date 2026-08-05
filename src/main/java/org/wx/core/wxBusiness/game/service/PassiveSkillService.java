package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PassiveCombatEffect;
import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.PassiveAnchorType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.PeriodicTriggerMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;
import org.wx.core.wxBusiness.game.mapper.PassiveSkillMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PassiveSkillService extends WxServiceImpl<PassiveSkillMapper, PassiveSkill> {

    @Resource
    private PassiveConditionService passiveConditionService;
    @Resource
    private PassiveEffectService passiveEffectService;
    @Resource
    private PassiveCombatEffectService passiveCombatEffectService;

    @Override
    public IPage<PassiveSkill> pageQuery(PassiveSkill entity) {
        IPage<PassiveSkill> page = super.pageQuery(entity);
        fillStackModeLabels(page.getRecords());
        return page;
    }

    public PassiveSkill getDetail(String id) {
        PassiveSkill skill = this.getById(id);
        ErrorFactory.notNull(skill, "被动技能不存在");
        skill.setConditions(passiveConditionService.listBySkillId(id));
        if (skill.getPassiveType() == PassiveSkillType.IN_ANCHOR || skill.getPassiveType() == PassiveSkillType.IN_PERIODIC) {
            skill.setCombatEffects(passiveCombatEffectService.listBySkillId(id));
            skill.setEffects(List.of());
            if (skill.getPassiveType() == PassiveSkillType.IN_ANCHOR) {
                skill.setStackModeLabel(anchorListLabel(skill.getAnchorType()));
            } else {
                skill.setStackModeLabel(periodicListLabel(skill.getPeriodicTriggerMode()));
            }
        } else {
            skill.setEffects(passiveEffectService.listBySkillId(id));
            skill.setCombatEffects(List.of());
            fillStackModeLabel(skill, skill.getEffects());
        }
        return skill;
    }

    private void fillStackModeLabels(List<PassiveSkill> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        List<String> outIds = new ArrayList<>();
        for (PassiveSkill s : skills) {
            if (s != null && StringUtils.hasText(s.getId()) && s.getPassiveType() != null && s.getPassiveType().isOutOfCombat()) {
                outIds.add(s.getId());
            }
        }
        List<PassiveEffect> all = outIds.isEmpty() ? List.of() : passiveEffectService.listBySkillIds(outIds);
        Map<String, List<PassiveEffect>> bySkill = new HashMap<>();
        for (PassiveEffect e : all) {
            if (e == null || !StringUtils.hasText(e.getSkillId())) {
                continue;
            }
            bySkill.computeIfAbsent(e.getSkillId(), k -> new ArrayList<>()).add(e);
        }
        for (PassiveSkill s : skills) {
            if (s == null) {
                continue;
            }
            if (s.getPassiveType() == PassiveSkillType.IN_ANCHOR) {
                s.setStackModeLabel(anchorListLabel(s.getAnchorType()));
            } else if (s.getPassiveType() == PassiveSkillType.IN_PERIODIC) {
                s.setStackModeLabel(periodicListLabel(s.getPeriodicTriggerMode()));
            } else {
                fillStackModeLabel(s, bySkill.get(s.getId()));
            }
        }
    }

    private static String anchorListLabel(PassiveAnchorType type) {
        return type != null ? type.getLabel() : "—";
    }

    private static String periodicListLabel(PeriodicTriggerMode mode) {
        return mode != null ? mode.getLabel() : "—";
    }

    private void fillStackModeLabel(PassiveSkill skill, List<PassiveEffect> effects) {
        if (skill == null) {
            return;
        }
        boolean add = false;
        boolean mult = false;
        if (effects != null) {
            for (PassiveEffect e : effects) {
                if (e == null || e.getAttrKey() == null) {
                    continue;
                }
                PassiveEffectAttrKey.StackMode mode = e.getAttrKey().getStackMode();
                if (mode == PassiveEffectAttrKey.StackMode.MULT_PERCENT) {
                    mult = true;
                } else {
                    add = true;
                }
            }
        }
        if (add && mult) {
            skill.setStackModeLabel("加法+叠乘");
        } else if (mult) {
            skill.setStackModeLabel("叠乘");
        } else if (add) {
            skill.setStackModeLabel("加法");
        } else {
            skill.setStackModeLabel("—");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveWithConditions(PassiveSkill entity) {
        ErrorFactory.notEmpty(entity.getName(), "请输入被动名称");
        ErrorFactory.notNull(entity.getPassiveType(), "被动类型不能为空");
        if (entity.getConditionMode() == null) {
            entity.setConditionMode(PassiveConditionMode.UNLIMITED);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnable() == null) {
            entity.setEnable(true);
        }

        List<PassiveCondition> conditions = entity.getConditions();
        if (entity.getConditionMode() == PassiveConditionMode.SELECT) {
            ErrorFactory.throwError(conditions == null || conditions.isEmpty(), "请至少添加一条生效条件");
            validateConditions(conditions);
        }

        boolean inCombat = entity.getPassiveType() == PassiveSkillType.IN_ANCHOR
                || entity.getPassiveType() == PassiveSkillType.IN_PERIODIC;
        if (entity.getPassiveType() == PassiveSkillType.IN_ANCHOR) {
            clearPeriodicFields(entity);
            validateAnchor(entity);
            List<PassiveCombatEffect> combatEffects = entity.getCombatEffects();
            ErrorFactory.throwError(combatEffects == null || combatEffects.isEmpty(), "请至少添加一条效果");
            validateCombatEffects(combatEffects);
        } else if (entity.getPassiveType() == PassiveSkillType.IN_PERIODIC) {
            clearAnchorFields(entity);
            validatePeriodic(entity);
            List<PassiveCombatEffect> combatEffects = entity.getCombatEffects();
            ErrorFactory.throwError(combatEffects == null || combatEffects.isEmpty(), "请至少添加一条效果");
            validateCombatEffects(combatEffects);
        } else {
            clearAnchorFields(entity);
            clearPeriodicFields(entity);
            List<PassiveEffect> effects = entity.getEffects();
            ErrorFactory.throwError(effects == null || effects.isEmpty(), "请至少添加一条效果");
            validateEffects(entity.getPassiveType(), effects);
        }

        this.saveOrUpdate(entity);
        ErrorFactory.notEmpty(entity.getId(), "被动技能保存失败");

        passiveConditionService.removeBySkillId(entity.getId());
        if (entity.getConditionMode() == PassiveConditionMode.SELECT && conditions != null) {
            int sort = 0;
            for (PassiveCondition c : conditions) {
                PassiveCondition row = new PassiveCondition();
                row.setSkillId(entity.getId());
                row.setConditionType(c.getConditionType());
                row.setRefItemId(c.getRefItemId());
                row.setRefItemType(c.getRefItemType());
                row.setRefSkillId(c.getRefSkillId());
                row.setRefSkillType(c.getRefSkillType());
                row.setLeftFormulaJson(c.getLeftFormulaJson());
                row.setCompareOp(c.getCompareOp());
                row.setRightFormulaJson(c.getRightFormulaJson());
                row.setSort(c.getSort() != null ? c.getSort() : sort);
                row.setRemark(c.getRemark());
                row.setMore(c.getMore());
                clearUnusedFields(row);
                passiveConditionService.save(row);
                sort++;
            }
        }

        if (inCombat) {
            passiveEffectService.removeBySkillId(entity.getId());
            passiveCombatEffectService.removeBySkillId(entity.getId());
            int effectSort = 0;
            for (PassiveCombatEffect e : entity.getCombatEffects()) {
                PassiveCombatEffect row = new PassiveCombatEffect();
                row.setSkillId(entity.getId());
                row.setName(e.getName());
                row.setTargetType(e.getTargetType());
                row.setEffectType(e.getEffectType());
                row.setAttrKey(e.getAttrKey());
                row.setAttrDir(e.getAttrDir());
                row.setFormulaJson(e.getFormulaJson());
                row.setHitSegments(e.getHitSegments() != null && e.getHitSegments() > 0 ? e.getHitSegments() : 1);
                row.setSort(e.getSort() != null ? e.getSort() : effectSort);
                row.setRemark(e.getRemark());
                row.setMore(e.getMore());
                if (e.getEffectType() != SkillEffectType.ATTR_MODIFY) {
                    row.setAttrKey(null);
                    row.setAttrDir(null);
                }
                passiveCombatEffectService.save(row);
                effectSort++;
            }
        } else {
            passiveCombatEffectService.removeBySkillId(entity.getId());
            passiveEffectService.removeBySkillId(entity.getId());
            int effectSort = 0;
            for (PassiveEffect e : entity.getEffects()) {
                PassiveEffect row = new PassiveEffect();
                row.setSkillId(entity.getId());
                row.setAttrKey(e.getAttrKey());
                row.setAttrDir(e.getAttrDir());
                row.setValueNum(e.getValueNum());
                row.setSort(e.getSort() != null ? e.getSort() : effectSort);
                row.setRemark(e.getRemark());
                row.setMore(e.getMore());
                passiveEffectService.save(row);
                effectSort++;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeWithConditions(String id) {
        ErrorFactory.notEmpty(id, "ID不能为空");
        passiveConditionService.removeBySkillId(id);
        passiveEffectService.removeBySkillId(id);
        passiveCombatEffectService.removeBySkillId(id);
        this.removeById(id);
    }

    private void clearAnchorFields(PassiveSkill entity) {
        entity.setAnchorType(null);
        entity.setSkillMatchMode(null);
        entity.setRefSkillType(null);
        entity.setRefSkillId(null);
    }

    private void clearPeriodicFields(PassiveSkill entity) {
        entity.setPeriodicTriggerMode(null);
        entity.setLeftFormulaJson(null);
        entity.setCompareOp(null);
        entity.setRightFormulaJson(null);
        entity.setMaxTriggerPerBattle(null);
    }

    private void validatePeriodic(PassiveSkill entity) {
        ErrorFactory.notNull(entity.getPeriodicTriggerMode(), "请选择触发模式");
        ErrorFactory.notEmpty(entity.getLeftFormulaJson(), "请配置上公式");
        ErrorFactory.throwError("[]".equals(entity.getLeftFormulaJson().trim()), "请配置上公式");
        ErrorFactory.notNull(entity.getCompareOp(), "请选择比较符");
        ErrorFactory.notEmpty(entity.getRightFormulaJson(), "请配置下公式");
        ErrorFactory.throwError("[]".equals(entity.getRightFormulaJson().trim()), "请配置下公式");
        if (entity.getMaxTriggerPerBattle() == null) {
            entity.setMaxTriggerPerBattle(0);
        }
        ErrorFactory.throwError(entity.getMaxTriggerPerBattle() < 0, "触发次数不能为负");
    }

    private void validateAnchor(PassiveSkill entity) {
        ErrorFactory.notNull(entity.getAnchorType(), "请选择锚点");
        PassiveAnchorType anchor = entity.getAnchorType();
        if (anchor.needsSkillMatch()) {
            ErrorFactory.notNull(entity.getSkillMatchMode(), "请选择技能匹配范围");
            SkillChargeMatchMode mode = entity.getSkillMatchMode();
            if (mode == SkillChargeMatchMode.ANY_TYPE) {
                ErrorFactory.notNull(entity.getRefSkillType(), "请选择技能类型");
                entity.setRefSkillId(null);
            } else if (mode == SkillChargeMatchMode.SPECIFIC) {
                ErrorFactory.notEmpty(entity.getRefSkillId(), "请选择指定技能");
                entity.setRefSkillType(null);
            } else {
                entity.setRefSkillType(null);
                entity.setRefSkillId(null);
            }
        } else {
            entity.setSkillMatchMode(null);
            entity.setRefSkillType(null);
            entity.setRefSkillId(null);
        }
    }

    private void validateCombatEffects(List<PassiveCombatEffect> effects) {
        for (PassiveCombatEffect e : effects) {
            ErrorFactory.notEmpty(e.getName(), "请输入效果名称");
            ErrorFactory.notNull(e.getTargetType(), "请选择效果目标");
            ErrorFactory.notNull(e.getEffectType(), "请选择效果类型");
            ErrorFactory.notEmpty(e.getFormulaJson(), "请配置效果公式");
            ErrorFactory.throwError("[]".equals(e.getFormulaJson().trim()), "请配置效果公式");
            if (e.getEffectType() == SkillEffectType.ATTR_MODIFY) {
                ErrorFactory.notNull(e.getAttrKey(), "请选择修改属性");
                ErrorFactory.notNull(e.getAttrDir(), "请选择增加/减少");
            }
            if (e.getHitSegments() != null) {
                ErrorFactory.throwError(e.getHitSegments() < 1, "段数至少为 1");
            }
        }
    }

    private void validateEffects(PassiveSkillType passiveType, List<PassiveEffect> effects) {
        for (PassiveEffect e : effects) {
            ErrorFactory.notNull(e.getAttrKey(), "请选择属性");
            ErrorFactory.notNull(e.getAttrDir(), "请选择增加/减少");
            ErrorFactory.notNull(e.getValueNum(), "请输入效果数值");
            ErrorFactory.throwError(e.getValueNum().compareTo(BigDecimal.ZERO) < 0, "效果数值不能为负数");
            PassiveEffectAttrKey key = e.getAttrKey();
            if (passiveType == PassiveSkillType.OUT_BASIC) {
                ErrorFactory.throwError(!key.isBasic(), "基础属性被动只能配置攻击/生命/防御");
            } else if (passiveType == PassiveSkillType.OUT_ADVANCED) {
                ErrorFactory.throwError(!key.isAdvanced(), "高级属性被动只能配置吸血/攻速/伤害比例/最终攻防血");
            }
        }
    }

    private void validateConditions(List<PassiveCondition> conditions) {
        for (PassiveCondition c : conditions) {
            ErrorFactory.notNull(c.getConditionType(), "请选择条件类型");
            PassiveConditionType type = c.getConditionType();
            switch (type) {
                case EQUIP_ITEM -> ErrorFactory.notEmpty(c.getRefItemId(), "请选择指定装备");
                case EQUIP_ITEM_TYPE -> ErrorFactory.notNull(c.getRefItemType(), "请选择装备类型");
                case EQUIP_SKILL -> ErrorFactory.notEmpty(c.getRefSkillId(), "请选择指定技能");
                case EQUIP_SKILL_TYPE -> ErrorFactory.notNull(c.getRefSkillType(), "请选择技能类型");
                case FORMULA_COMPARE -> {
                    ErrorFactory.notEmpty(c.getLeftFormulaJson(), "请配置左侧公式");
                    ErrorFactory.notNull(c.getCompareOp(), "请选择比较符");
                    ErrorFactory.notEmpty(c.getRightFormulaJson(), "请配置右侧公式");
                    ErrorFactory.throwError("[]".equals(c.getLeftFormulaJson().trim()), "请配置左侧公式");
                    ErrorFactory.throwError("[]".equals(c.getRightFormulaJson().trim()), "请配置右侧公式");
                }
                default -> ErrorFactory.throwError(true, "未知条件类型");
            }
        }
    }

    private void clearUnusedFields(PassiveCondition row) {
        PassiveConditionType type = row.getConditionType();
        if (type != PassiveConditionType.EQUIP_ITEM) {
            row.setRefItemId(null);
        }
        if (type != PassiveConditionType.EQUIP_ITEM_TYPE) {
            row.setRefItemType(null);
        }
        if (type != PassiveConditionType.EQUIP_SKILL) {
            row.setRefSkillId(null);
        }
        if (type != PassiveConditionType.EQUIP_SKILL_TYPE) {
            row.setRefSkillType(null);
        }
        if (type != PassiveConditionType.FORMULA_COMPARE) {
            row.setLeftFormulaJson(null);
            row.setCompareOp(null);
            row.setRightFormulaJson(null);
        }
    }
}
