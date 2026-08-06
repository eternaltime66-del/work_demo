package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

/**
 * 单位技能槽快照（供前端充能环：普 / 技 / 大）
 */
@Data
public class BattleSkillSlotVo {

    private String skillId;
    private String skillType;
    private String name;
    /** 所需充能；0 表示无需充能（常显满环） */
    private int need;
    /** ACTION_VALUE 充能：每经过多少行动值 */
    private Integer avEvery;
    /** ACTION_VALUE 充能：每次增加点数 */
    private Integer avGain;
}
