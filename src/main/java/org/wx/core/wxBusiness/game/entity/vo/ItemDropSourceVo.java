package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDropSourceVo {

    private String levelId;
    private String displayCode;
    private String levelName;
    private String chapterName;
    private String monsterId;
    private String monsterName;
    private BigDecimal dropRate;
    private Integer minQty;
    private Integer maxQty;
}
