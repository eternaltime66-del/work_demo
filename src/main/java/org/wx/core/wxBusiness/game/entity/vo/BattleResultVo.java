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
    private List<MonsterDropResultVo> drops = new ArrayList<>();
    /** 开战时单位快照（血条） */
    private List<BattleUnitSnapVo> units = new ArrayList<>();
}
