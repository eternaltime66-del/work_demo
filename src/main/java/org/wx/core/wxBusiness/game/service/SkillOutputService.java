package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.battle.SkillOutputDescUnit;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.DamageElement;
import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;
import org.wx.core.wxBusiness.game.mapper.SkillOutputMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SkillOutputService extends WxServiceImpl<SkillOutputMapper, SkillOutput> {

    @Resource
    private BuffDefService buffDefService;

    public List<SkillOutput> listBySkillId(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return Collections.emptyList();
        }
        List<SkillOutput> list = this.find()
                .eq(SkillOutput::getSkillId, skillId)
                .orderByAsc(SkillOutput::getSort)
                .list();
        fillBuffNames(list);
        return list;
    }

    public List<SkillOutput> listByPassiveSkillId(String passiveSkillId) {
        if (passiveSkillId == null || passiveSkillId.isBlank()) {
            return Collections.emptyList();
        }
        List<SkillOutput> list = this.find()
                .eq(SkillOutput::getPassiveSkillId, passiveSkillId)
                .orderByAsc(SkillOutput::getSort)
                .list();
        fillBuffNames(list);
        return list;
    }

    private void fillBuffNames(List<SkillOutput> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> ids = new HashSet<>();
        for (SkillOutput o : list) {
            if (o != null && o.getBuffDefId() != null && !o.getBuffDefId().isBlank()) {
                ids.add(o.getBuffDefId());
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        Map<String, String> names = new HashMap<>();
        for (BuffDef def : buffDefService.listByIds(ids)) {
            if (def != null && def.getId() != null) {
                names.put(def.getId(), def.getName());
            }
        }
        for (SkillOutput o : list) {
            if (o != null && o.getBuffDefId() != null) {
                o.setBuffDefName(names.get(o.getBuffDefId()));
            }
        }
    }

    public void removeBySkillId(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return;
        }
        this.remove(new LambdaQueryWrapper<SkillOutput>().eq(SkillOutput::getSkillId, skillId));
    }

    public void removeByPassiveSkillId(String passiveSkillId) {
        if (passiveSkillId == null || passiveSkillId.isBlank()) {
            return;
        }
        this.remove(new LambdaQueryWrapper<SkillOutput>().eq(SkillOutput::getPassiveSkillId, passiveSkillId));
    }

    public void validateOutputs(List<SkillOutput> outputs) {
        ErrorFactory.throwError(outputs == null || outputs.isEmpty(), "请至少添加一条输出");
        for (SkillOutput e : outputs) {
            ErrorFactory.notNull(e, "输出不能为空");
            ErrorFactory.notNull(e.getOutputKind(), "请选择输出类型");
            ErrorFactory.notNull(e.getTargetType(), "请选择目标");
            if (e.getOutputKind() == SkillOutputKind.ATTR) {
                ErrorFactory.notNull(e.getAttrKey(), "请选择属性");
                ErrorFactory.notNull(e.getAttrDir(), "请选择增减");
                ErrorFactory.notEmpty(e.getFormulaJson(), "请配置公式");
            } else if (e.getOutputKind() == SkillOutputKind.EFFECT) {
                ErrorFactory.notNull(e.getEffectType(), "请选择伤害/治疗");
                ErrorFactory.notEmpty(e.getFormulaJson(), "请配置公式");
            } else if (e.getOutputKind() == SkillOutputKind.APPEND_BUFF) {
                ErrorFactory.notEmpty(e.getBuffDefId(), "请选择 BUFF");
            }
            e.setName(SkillOutputDescUnit.autoName(e));
        }
    }

    /** 覆盖写入主动技能输出（skillId 绑定，清空旧行） */
    @Transactional(rollbackFor = Exception.class)
    public void replaceForActiveSkill(String skillId, List<SkillOutput> outputs) {
        ErrorFactory.notEmpty(skillId, "技能不能为空");
        validateOutputs(outputs);
        removeBySkillId(skillId);
        int sort = 0;
        for (SkillOutput e : outputs) {
            SkillOutput row = copyClean(e);
            row.setId(null);
            row.setSkillId(skillId);
            row.setPassiveSkillId(null);
            row.setSort(e.getSort() != null ? e.getSort() : sort);
            this.save(row);
            sort++;
        }
    }

    public SkillOutput copyClean(SkillOutput e) {
        SkillOutput row = new SkillOutput();
        row.setName(e.getName());
        row.setOutputKind(e.getOutputKind());
        row.setTargetType(e.getTargetType());
        row.setAttrKey(e.getAttrKey());
        row.setAttrDir(e.getAttrDir());
        row.setEffectType(e.getEffectType());
        row.setDamageElement(e.getDamageElement() != null ? e.getDamageElement() : DamageElement.PHYSICAL);
        row.setFormulaJson(e.getFormulaJson());
        row.setHitSegments(e.getHitSegments() != null && e.getHitSegments() > 0 ? e.getHitSegments() : 1);
        row.setTriggerRate(e.getTriggerRate() != null ? e.getTriggerRate() : 100);
        row.setDurationAv(e.getDurationAv() != null ? e.getDurationAv() : 0);
        row.setBuffDefId(e.getBuffDefId());
        row.setRemark(e.getRemark());
        row.setMore(e.getMore());
        return row;
    }
}
