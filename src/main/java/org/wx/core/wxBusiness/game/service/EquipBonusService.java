package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.vo.EquipBonusVo;

/**
 * 汇总玩家当前装备的攻击/生命/防御加成
 */
@Service
public class EquipBonusService {

    @Resource
    private PlayerEquipService playerEquipService;
    @Resource
    private ItemService itemService;
    @Resource
    private ItemWeaponService itemWeaponService;
    @Resource
    private ItemArmorService itemArmorService;
    @Resource
    private ItemGlovesService itemGlovesService;
    @Resource
    private ItemHelmetService itemHelmetService;
    @Resource
    private ItemLegsService itemLegsService;

    public EquipBonusVo sumBonus(String uid) {
        EquipBonusVo bonus = new EquipBonusVo();
        if (Wx.isEmpty(uid)) {
            return bonus;
        }
        PlayerEquip equip = playerEquipService.find().eq(PlayerEquip::getUid, uid).one();
        if (equip == null) {
            return bonus;
        }
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            if (Wx.isEmpty(itemId)) {
                continue;
            }
            addItemBonus(bonus, itemId);
        }
        return bonus;
    }

    private void addItemBonus(EquipBonusVo bonus, String itemId) {
        Item item = itemService.getById(itemId);
        if (item == null || item.getItemType() == null) {
            return;
        }
        ItemType type = item.getItemType();
        switch (type) {
            case WEAPON -> {
                ItemWeapon ext = itemWeaponService.getByItemId(itemId);
                if (ext != null) {
                    bonus.setAtk(bonus.getAtk() + nvl(ext.getBaseAtk()));
                }
            }
            case ARMOR -> {
                ItemArmor ext = itemArmorService.getByItemId(itemId);
                if (ext != null) {
                    bonus.setHp(bonus.getHp() + nvl(ext.getHp()));
                    bonus.setDefense(bonus.getDefense() + nvl(ext.getDefense()));
                }
            }
            case GLOVES -> {
                ItemGloves ext = itemGlovesService.getByItemId(itemId);
                if (ext != null) {
                    bonus.setHp(bonus.getHp() + nvl(ext.getHp()));
                    bonus.setDefense(bonus.getDefense() + nvl(ext.getDefense()));
                }
            }
            case HELMET -> {
                ItemHelmet ext = itemHelmetService.getByItemId(itemId);
                if (ext != null) {
                    bonus.setHp(bonus.getHp() + nvl(ext.getHp()));
                    bonus.setDefense(bonus.getDefense() + nvl(ext.getDefense()));
                }
            }
            case LEGS -> {
                ItemLegs ext = itemLegsService.getByItemId(itemId);
                if (ext != null) {
                    bonus.setHp(bonus.getHp() + nvl(ext.getHp()));
                    bonus.setDefense(bonus.getDefense() + nvl(ext.getDefense()));
                }
            }
            default -> {
            }
        }
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
