package org.wx.core.wxBusiness.game.entity.enums;

import org.wx.core.wxBusiness.game.entity.PlayerEquip;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主角装备槽位
 */
public enum EquipSlot {

    WEAPON("武器", ItemType.WEAPON),
    ARMOR("护甲", ItemType.ARMOR),
    GLOVES("护手", ItemType.GLOVES),
    HELMET("头盔", ItemType.HELMET),
    LEGS("护腿", ItemType.LEGS),
    ACCESSORY_1("饰品1", ItemType.ACCESSORY),
    ACCESSORY_2("饰品2", ItemType.ACCESSORY),
    ACCESSORY_3("饰品3", ItemType.ACCESSORY);

    private final String label;
    private final ItemType requiredType;

    EquipSlot(String label, ItemType requiredType) {
        this.label = label;
        this.requiredType = requiredType;
    }

    public String getLabel() {
        return label;
    }

    public ItemType getRequiredType() {
        return requiredType;
    }

    public static EquipSlot parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static List<EquipSlot> forType(ItemType type) {
        return Arrays.stream(values())
                .filter(slot -> slot.requiredType == type)
                .collect(Collectors.toList());
    }

    public String getItemId(PlayerEquip equip) {
        if (equip == null) {
            return null;
        }
        return switch (this) {
            case WEAPON -> equip.getWeaponItemId();
            case ARMOR -> equip.getArmorItemId();
            case GLOVES -> equip.getGlovesItemId();
            case HELMET -> equip.getHelmetItemId();
            case LEGS -> equip.getLegsItemId();
            case ACCESSORY_1 -> equip.getAccessory1ItemId();
            case ACCESSORY_2 -> equip.getAccessory2ItemId();
            case ACCESSORY_3 -> equip.getAccessory3ItemId();
        };
    }

    public void setItemId(PlayerEquip equip, String itemId) {
        if (equip == null) {
            return;
        }
        switch (this) {
            case WEAPON -> equip.setWeaponItemId(itemId);
            case ARMOR -> equip.setArmorItemId(itemId);
            case GLOVES -> equip.setGlovesItemId(itemId);
            case HELMET -> equip.setHelmetItemId(itemId);
            case LEGS -> equip.setLegsItemId(itemId);
            case ACCESSORY_1 -> equip.setAccessory1ItemId(itemId);
            case ACCESSORY_2 -> equip.setAccessory2ItemId(itemId);
            case ACCESSORY_3 -> equip.setAccessory3ItemId(itemId);
        }
    }
}
