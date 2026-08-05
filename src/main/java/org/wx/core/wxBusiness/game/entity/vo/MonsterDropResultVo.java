package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;

/**
 * 单次掉落结算条目
 */
@Data
public class MonsterDropResultVo {

    private String monsterId;
    private String itemId;
    private String itemName;
    private String icon;
    private ItemType itemType;
    private Integer quantity;
}
