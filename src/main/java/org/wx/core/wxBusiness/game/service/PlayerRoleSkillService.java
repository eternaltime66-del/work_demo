package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.PlayerRoleSkill;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.mapper.PlayerRoleSkillMapper;

import java.util.List;
import java.util.ArrayList;

@Service
public class PlayerRoleSkillService extends WxServiceImpl<PlayerRoleSkillMapper, PlayerRoleSkill> {

    @Resource
    private ActiveSkillService activeSkillService;

    public List<PlayerRoleSkill> listByRoleId(String roleId) {
        return this.find().eq(PlayerRoleSkill::getRoleId, roleId).orderByAsc(PlayerRoleSkill::getSort).list();
    }

    public List<ActiveSkill> listSkillsByRoleId(String roleId) {
        List<PlayerRoleSkill> binds = listByRoleId(roleId);
        List<ActiveSkill> skills = new ArrayList<>(binds.stream()
                .map(b -> activeSkillService.getById(b.getSkillId()))
                .filter(s -> s != null && !Boolean.FALSE.equals(s.getEnable()))
                .toList());
        return appendGlobalNormalFallback(skills);
    }

    List<ActiveSkill> appendGlobalNormalFallback(List<ActiveSkill> source) {
        List<ActiveSkill> skills = source == null ? new ArrayList<>() : new ArrayList<>(source);
        boolean hasNormal = skills.stream().anyMatch(s -> s != null && s.getSkillType() == ActiveSkillType.NORMAL);
        if (!hasNormal) {
            ActiveSkill fallback = activeSkillService.ensureDefaultNormalSkill();
            if (fallback != null) {
                skills.add(0, fallback);
            }
        }
        return skills;
    }

    @Transactional(rollbackFor = Exception.class)
    public void grantSkillIfAbsent(String roleId, String skillId, int sort) {
        if (Wx.isEmpty(roleId) || Wx.isEmpty(skillId)) {
            return;
        }
        long exists = this.find()
                .eq(PlayerRoleSkill::getRoleId, roleId)
                .eq(PlayerRoleSkill::getSkillId, skillId)
                .count();
        if (exists > 0) {
            return;
        }
        PlayerRoleSkill row = new PlayerRoleSkill();
        row.setRoleId(roleId);
        row.setSkillId(skillId);
        row.setSort(sort);
        this.save(row);
    }

}
