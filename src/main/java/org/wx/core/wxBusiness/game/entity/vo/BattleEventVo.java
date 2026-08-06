package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗结构化事件（与 logs 并行，供前端特效/时间轴回放）
 */
@Data
public class BattleEventVo {

    /** 全局行动值时间戳 */
    private int t;

    /** BATTLE_START / TURN_START / CAST / HIT / HEAL / CHARGE / BUFF / DEATH / BATTLE_END */
    private String type;

    private String uid;
    private String target;
    private List<String> targets = new ArrayList<>();

    private String skillId;
    private String skillName;
    private String skillType;
    private String element;
    private String shape;

    private Integer seg;
    private Integer segTotal;
    private Integer damage;
    private Boolean crit;
    private Integer hpAfter;
    private Integer maxHp;

    /** CHARGE：当前 / 所需 */
    private Integer cur;
    private Integer max;

    private String attrKey;
    /** UP / DOWN */
    private String dir;
    private Integer value;
    private Integer duration;
    private Integer stack;

    private Integer actionValue;
    private String outcome;
    private List<MonsterDropResultVo> drops = new ArrayList<>();
}
