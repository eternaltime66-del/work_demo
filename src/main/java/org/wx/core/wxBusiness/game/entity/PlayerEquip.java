package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 玩家装备（挂主角）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_equip")
@BizIdPrefix("PEQ")
public class PlayerEquip extends WxBaseEntity<PlayerEquip> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;

    private String weaponItemId;

    private String armorItemId;

    private String glovesItemId;

    private String helmetItemId;

    private String legsItemId;

    private String accessory1ItemId;

    private String accessory2ItemId;

    private String accessory3ItemId;

    private String skill1ItemId;
    private String skill2ItemId;
    private String skill3ItemId;
    private String skill4ItemId;
    private String skill5ItemId;
    private String skill6ItemId;
    private String skill7ItemId;
    private String skill8ItemId;

    public List<String> listEquippedItemIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(this);
            if (itemId != null && !itemId.isBlank()) {
                ids.add(itemId);
            }
        }
        return new ArrayList<>(ids);
    }
}
