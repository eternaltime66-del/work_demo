package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
}
