package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StageLevelPreviewVo {
    private String levelId;
    private String name;
    private String code;
    /** 是否已通关（已首通） */
    private Boolean cleared;
    private Boolean firstRewardClaimed;
    private Integer fightStaminaCost;
    private Integer dailyMaxAttempts;
    private Integer todayAttemptCount;
    private Integer todayAttemptLeft;
    private Boolean unlocked;

    private List<StageFirstRewardVo> firstRewards = new ArrayList<>();
    /** 本关怪物可能掉落（去重合并，展示用） */
    private List<StageDropPreviewVo> drops = new ArrayList<>();
}
