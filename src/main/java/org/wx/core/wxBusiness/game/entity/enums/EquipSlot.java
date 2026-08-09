package org.wx.core.wxBusiness.game.entity.enums;

import org.wx.core.wxBusiness.game.entity.PlayerEquip;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** 主角装备槽位。 */
public enum EquipSlot {
    WEAPON("武器", ItemType.WEAPON),
    ARMOR("护甲", ItemType.ARMOR),
    GLOVES("护手", ItemType.GLOVES),
    HELMET("头盔", ItemType.HELMET),
    LEGS("护腿", ItemType.LEGS),
    ACCESSORY_1("饰品1", ItemType.ACCESSORY),
    ACCESSORY_2("饰品2", ItemType.ACCESSORY),
    ACCESSORY_3("饰品3", ItemType.ACCESSORY),
    SKILL_1("技能1", ItemType.SKILL_STONE),
    SKILL_2("技能2", ItemType.SKILL_STONE),
    SKILL_3("技能3", ItemType.SKILL_STONE),
    SKILL_4("技能4", ItemType.SKILL_STONE),
    SKILL_5("技能5", ItemType.SKILL_STONE),
    SKILL_6("技能6", ItemType.SKILL_STONE),
    SKILL_7("技能7", ItemType.SKILL_STONE),
    SKILL_8("技能8", ItemType.SKILL_STONE);

    private final String label;
    private final ItemType requiredType;

    EquipSlot(String label, ItemType requiredType) {
        this.label = label;
        this.requiredType = requiredType;
    }

    public String getLabel() { return label; }
    public ItemType getRequiredType() { return requiredType; }

    public static EquipSlot parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return valueOf(raw.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    public static List<EquipSlot> forType(ItemType type) {
        return Arrays.stream(values()).filter(slot -> slot.requiredType == type).collect(Collectors.toList());
    }

    public String getItemId(PlayerEquip equip) {
        if (equip == null) return null;
        return switch (this) {
            case WEAPON -> equip.getWeaponItemId();
            case ARMOR -> equip.getArmorItemId();
            case GLOVES -> equip.getGlovesItemId();
            case HELMET -> equip.getHelmetItemId();
            case LEGS -> equip.getLegsItemId();
            case ACCESSORY_1 -> equip.getAccessory1ItemId();
            case ACCESSORY_2 -> equip.getAccessory2ItemId();
            case ACCESSORY_3 -> equip.getAccessory3ItemId();
            case SKILL_1 -> equip.getSkill1ItemId();
            case SKILL_2 -> equip.getSkill2ItemId();
            case SKILL_3 -> equip.getSkill3ItemId();
            case SKILL_4 -> equip.getSkill4ItemId();
            case SKILL_5 -> equip.getSkill5ItemId();
            case SKILL_6 -> equip.getSkill6ItemId();
            case SKILL_7 -> equip.getSkill7ItemId();
            case SKILL_8 -> equip.getSkill8ItemId();
        };
    }

    public void setItemId(PlayerEquip equip, String itemId) {
        if (equip == null) return;
        switch (this) {
            case WEAPON -> equip.setWeaponItemId(itemId);
            case ARMOR -> equip.setArmorItemId(itemId);
            case GLOVES -> equip.setGlovesItemId(itemId);
            case HELMET -> equip.setHelmetItemId(itemId);
            case LEGS -> equip.setLegsItemId(itemId);
            case ACCESSORY_1 -> equip.setAccessory1ItemId(itemId);
            case ACCESSORY_2 -> equip.setAccessory2ItemId(itemId);
            case ACCESSORY_3 -> equip.setAccessory3ItemId(itemId);
            case SKILL_1 -> equip.setSkill1ItemId(itemId);
            case SKILL_2 -> equip.setSkill2ItemId(itemId);
            case SKILL_3 -> equip.setSkill3ItemId(itemId);
            case SKILL_4 -> equip.setSkill4ItemId(itemId);
            case SKILL_5 -> equip.setSkill5ItemId(itemId);
            case SKILL_6 -> equip.setSkill6ItemId(itemId);
            case SKILL_7 -> equip.setSkill7ItemId(itemId);
            case SKILL_8 -> equip.setSkill8ItemId(itemId);
        }
    }
}
