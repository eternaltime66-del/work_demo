package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StageDropPreviewVo {
    private String itemId;
    private String itemName;
    private BigDecimal dropRate;
    private Integer minQty;
    private Integer maxQty;
}
