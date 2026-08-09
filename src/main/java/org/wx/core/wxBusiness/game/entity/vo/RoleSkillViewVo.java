package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

/**
 * 角色详情中的技能一览项。
 */
@Data
public class RoleSkillViewVo {

    private String skillId;

    private String skillName;

    /** NORMAL / SMALL / ULTIMATE */
    private String skillType;

    /** ROLE / WEAPON / EQUIP */
    private String source;

    private String sourceLabel;
}
