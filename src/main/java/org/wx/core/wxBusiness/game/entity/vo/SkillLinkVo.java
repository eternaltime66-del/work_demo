package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

/**
 * 物品详情里可点击的技能链接
 */
@Data
public class SkillLinkVo {

    private String id;
    private String name;
    private String skillType;

    public static SkillLinkVo of(String id, String name, String skillType) {
        SkillLinkVo vo = new SkillLinkVo();
        vo.setId(id);
        vo.setName(name);
        vo.setSkillType(skillType);
        return vo;
    }
}
