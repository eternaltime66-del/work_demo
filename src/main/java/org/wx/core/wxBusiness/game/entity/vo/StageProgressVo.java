package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StageProgressVo {
    private String id;
    private String parentId;
    private String kind;
    private String name;
    private String code;
    private Integer sort;
    private Boolean enable;
    private Integer staminaCost;
    private Integer dailyMaxAttempts;

    private Boolean unlocked;
    private Boolean cleared;
    private Boolean firstRewardClaimed;
    /** 未首通为 0；已首通为配置体力 */
    private Integer fightStaminaCost;
    private Integer todayAttemptCount;
    private Integer todayAttemptLeft;
    private List<StageFirstRewardVo> firstRewards = new ArrayList<>();
    private List<StageProgressVo> chapters;
    private List<StageProgressVo> levels;
}
