package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

/**
 * 备战人偶两侧技能槽（1~8）展示。
 */
@Data
public class PrepSkillSlotVo {

    /** 1~8 */
    private Integer slotNo;

    private boolean empty = true;

    private String skillId;

    private String skillName;

    /** NORMAL / SMALL / ULTIMATE */
    private String skillType;
}
