package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.PassiveCondition;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.BattleStartApplyRule;
import org.wx.core.wxBusiness.game.entity.enums.CompareOp;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionMode;
import org.wx.core.wxBusiness.game.entity.enums.PassiveConditionType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.SkillChargeMatchMode;
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
    private SkillOutputService skillOutputService;
    @Resource
    private ItemService itemService;
    @Resource
    private ActiveSkillService activeSkillService;

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
        fillConditionNames(skill.getConditions());
        if (skill.getPassiveType() != null && skill.getPassiveType().isBattlePassive()) {
            skill.setOutputs(skillOutputService.listByPassiveSkillId(id));
            skill.setEffects(List.of());
            skill.setStackModeLabel(skill.getPassiveType().label());
        } else {
            skill.setEffects(passiveEffectService.listBySkillId(id));
            skill.setOutputs(List.of());
            fillStackModeLabel(skill, skill.getEffects());
        }
        return skill;
    }

    private void fillConditionNames(List<PassiveCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return;
        }
        for (PassiveCondition c : conditions) {
            if (c == null) {
                continue;
            }
            if (StringUtils.hasText(c.getRefItemId())) {
                Item item = itemService.getById(c.getRefItemId());
                if (item != null) {
                    c.setRefItemName(item.getName());
                }
            }
            if (StringUtils.hasText(c.getRefSkillId())) {
                ActiveSkill sk = activeSkillService.getById(c.getRefSkillId());
                if (sk != null) {
                    c.setRefSkillName(sk.getName());
                }
            }
        }
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
            if (s.getPassiveType() != null && s.getPassiveType().isBattlePassive()) {
                s.setStackModeLabel(s.getPassiveType().label());
            } else {
                fillStackModeLabel(s, bySkill.get(s.getId()));
            }
        }
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

        boolean battle = entity.getPassiveType().isBattlePassive();
        if (battle) {
            validateBattlePassive(entity);
            List<SkillOutput> outputs = entity.getOutputs();
            ErrorFactory.throwError(outputs == null || outputs.isEmpty(), "请至少添加一条输出");
            validateOutputs(outputs);
        } else {
            entity.setCombatEvent(null);
            entity.setStartApplyRule(null);
            entity.setStartElapsedAv(null);
            entity.setLeftFormulaJson(null);
            entity.setRightFormulaJson(null);
            entity.setCompareOp(null);
            entity.setMaxTriggerPerBattle(null);
            entity.setSkillMatchMode(null);
            entity.setRefSkillId(null);
            entity.setRefSkillType(null);
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

        if (battle) {
            passiveEffectService.removeBySkillId(entity.getId());
            skillOutputService.removeByPassiveSkillId(entity.getId());
            int effectSort = 0;
            for (SkillOutput e : entity.getOutputs()) {
                SkillOutput row = copyOutput(e);
                row.setId(null);
                row.setPassiveSkillId(entity.getId());
                row.setSkillId(null);
                row.setSort(e.getSort() != null ? e.getSort() : effectSort);
                skillOutputService.save(row);
                effectSort++;
            }
        } else {
            skillOutputService.removeByPassiveSkillId(entity.getId());
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

    private SkillOutput copyOutput(SkillOutput e) {
        SkillOutput row = new SkillOutput();
        row.setName(e.getName());
        row.setOutputKind(e.getOutputKind());
        row.setTargetType(e.getTargetType());
        row.setAttrKey(e.getAttrKey());
        row.setAttrDir(e.getAttrDir());
        row.setEffectType(e.getEffectType());
        row.setDamageElement(e.getDamageElement());
        row.setFormulaJson(e.getFormulaJson());
        row.setHitSegments(e.getHitSegments() != null && e.getHitSegments() > 0 ? e.getHitSegments() : 1);
        row.setTriggerRate(e.getTriggerRate() != null ? e.getTriggerRate() : 100);
        row.setDurationAv(e.getDurationAv() != null ? e.getDurationAv() : 0);
        row.setBuffDefId(e.getBuffDefId());
        row.setRemark(e.getRemark());
        row.setMore(e.getMore());
        return row;
    }

    private void validateBattlePassive(PassiveSkill entity) {
        PassiveSkillType t = entity.getPassiveType();
        if (t == PassiveSkillType.BATTLE_JUDGE || t == PassiveSkillType.BATTLE_PULSE) {
            ErrorFactory.notEmpty(entity.getLeftFormulaJson(), "请配置左公式");
            ErrorFactory.notEmpty(entity.getRightFormulaJson(), "请配置右公式");
            // 脉冲固定「每当达到」= GTE；判定需选比较符
            if (t == PassiveSkillType.BATTLE_PULSE) {
                entity.setCompareOp(CompareOp.GTE);
            } else {
                ErrorFactory.notNull(entity.getCompareOp(), "请选择比较符");
            }
            entity.setStartApplyRule(null);
            entity.setStartElapsedAv(null);
            entity.setCombatEvent(null);
        } else if (t == PassiveSkillType.BATTLE_COMBAT) {
            ErrorFactory.notNull(entity.getCombatEvent(), "请选择战斗事件");
            entity.setStartApplyRule(null);
            entity.setLeftFormulaJson(null);
            entity.setRightFormulaJson(null);
            if (entity.getCombatEvent() != null
                    && (entity.getCombatEvent().name().contains("CAST") || entity.getCombatEvent().name().contains("RECEIVE"))) {
                if (entity.getSkillMatchMode() == null) {
                    entity.setSkillMatchMode(SkillChargeMatchMode.ANY);
                }
            }
        } else if (t == PassiveSkillType.BATTLE_START) {
            if (entity.getStartApplyRule() == null) {
                entity.setStartApplyRule(BattleStartApplyRule.IMMEDIATE);
            }
            entity.setCombatEvent(null);
            entity.setLeftFormulaJson(null);
            entity.setRightFormulaJson(null);
            if (entity.getStartApplyRule() != BattleStartApplyRule.IMMEDIATE) {
                ErrorFactory.throwError(entity.getStartElapsedAv() == null || entity.getStartElapsedAv() <= 0,
                        "请配置行动值间隔/阈值");
            }
        }
    }

    private void validateOutputs(List<SkillOutput> outputs) {
        skillOutputService.validateOutputs(outputs);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeWithConditions(String id) {
        ErrorFactory.notEmpty(id, "ID不能为空");
        passiveConditionService.removeBySkillId(id);
        passiveEffectService.removeBySkillId(id);
        skillOutputService.removeByPassiveSkillId(id);
        this.removeById(id);
    }

    private void validateEffects(PassiveSkillType passiveType, List<PassiveEffect> effects) {
        for (PassiveEffect e : effects) {
            ErrorFactory.notNull(e.getAttrKey(), "请选择属性");
            ErrorFactory.notNull(e.getAttrDir(), "请选择增加/减少");
            ErrorFactory.notNull(e.getValueNum(), "请输入效果数值");
            ErrorFactory.throwError(e.getValueNum().compareTo(BigDecimal.ZERO) < 0, "效果数值不能为负数");
            PassiveEffectAttrKey key = e.getAttrKey();
            if (passiveType == PassiveSkillType.OUT_BASIC) {
                ErrorFactory.throwError(!key.isBasic(), "基础型·基础属性只能配置攻击/生命/防御");
            } else if (passiveType == PassiveSkillType.OUT_ADVANCED) {
                ErrorFactory.throwError(!key.isAdvanced(), "基础型·高级属性只能配置吸血/攻速/伤害比例/最终攻防血");
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
