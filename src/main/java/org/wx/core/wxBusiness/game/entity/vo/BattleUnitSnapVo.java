package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

/**
 * 开战时单位血量快照（供前端血条回放）
 */
@Data
public class BattleUnitSnapVo {

    private String unitId;
    private String sourceId;
    private String name;
    /** ALLY / ENEMY */
    private String side;
    private int maxHp;
    private int hp;
}
