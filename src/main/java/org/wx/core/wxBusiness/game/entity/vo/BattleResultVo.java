package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
public class BattleResultVo {

    /** WIN / LOSE / DRAW */
    private String outcome;
    private int ticks;
    private List<String> logs = new ArrayList<>();
    /** 结构化事件时间轴（与 logs 并行） */
    private List<BattleEventVo> events = new ArrayList<>();
    private List<MonsterDropResultVo> drops = new ArrayList<>();
    /** 开战时单位快照（血条 / 站位） */
    private List<BattleUnitSnapVo> units = new ArrayList<>();

    /** 小关首通奖励（本次发放） */
    private List<StageFirstRewardVo> levelFirstRewards = new ArrayList<>();
    /** 大关首通奖励（本次发放） */
    private List<StageFirstRewardVo> chapterFirstRewards = new ArrayList<>();
    private Boolean firstClear;
    private Integer staminaLeft;
    private Integer staminaMax;

    /** 无尽塔：本战后状态 */
    private String towerStatus;
    private String towerNextLevelId;
    private String towerNextLevelCode;
    /** roleId -> 战后剩余 HP（爬塔续关用） */
    private Map<String, Integer> allyRemainHp;
}
