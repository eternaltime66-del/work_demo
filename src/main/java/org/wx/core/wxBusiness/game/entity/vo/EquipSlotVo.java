package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;

/**
 * 装备槽概览
 */
@Data
public class EquipSlotVo {

    private String slot;
    private String label;
    private ItemType requiredType;
    private String itemId;
    private String itemName;
    private String icon;
    private Boolean empty;
    /** 已装备物品的完整属性（攻击/生命/技能等） */
    private CraftItemDetailVo detail;

    public static EquipSlotVo of(EquipSlot slot, String itemId, String itemName, String icon) {
        EquipSlotVo vo = new EquipSlotVo();
        vo.setSlot(slot.name());
        vo.setLabel(slot.getLabel());
        vo.setRequiredType(slot.getRequiredType());
        vo.setItemId(itemId);
        vo.setItemName(itemName);
        vo.setIcon(icon);
        vo.setEmpty(itemId == null || itemId.isBlank());
        return vo;
    }
}
