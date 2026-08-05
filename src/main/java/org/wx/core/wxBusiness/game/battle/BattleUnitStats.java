package org.wx.core.wxBusiness.game.battle;

import lombok.Data;

/**
 * 单个角色在战斗中的基础属性快照（多角色 × 多属性）
 */
@Data
public class BattleUnitStats {

    private String roleId;
    private String roleName;

    private int maxHp;
    private int hp;
    private int atk;
    private int def;
    /** 角色行动值属性（非「已经过行动值」） */
    private int action;
}
