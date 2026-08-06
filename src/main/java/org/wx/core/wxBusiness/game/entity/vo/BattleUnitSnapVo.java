package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 开战时单位快照（供前端血条 / 时间轴 / 站位回放）
 */
@Data
public class BattleUnitSnapVo {

    /** 与 unitId 同值，前端优先用 uid */
    private String uid;
    private String unitId;
    private String sourceId;
    private String name;
    /** ALLY / ENEMY */
    private String side;
    private int maxHp;
    private int hp;

    private Integer posCol;
    private Integer posRow;
    private Integer gridW;
    private Integer gridH;
    /** 行动阈值（攻速周期） */
    private Integer atkSpeed;
    private String code;
    private String rarity;

    /** 普攻 / 小技能 / 大招槽（有则展示充能环） */
    private List<BattleSkillSlotVo> skills = new ArrayList<>();
}
