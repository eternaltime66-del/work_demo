package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

@Data
public class StageFirstRewardVo {
    private String id;
    private String stageId;
    private String itemId;
    private String itemName;
    private Integer qty;
    private Integer sort;
}
