package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.battle.AtkSpeedCalcUnit;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemAccessory;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 收集各来源攻速增减并叠乘得出最终行动值（仅角色 / 武器 / 饰品）
 */
@Service
public class AtkSpeedService {

    @Resource
    private PlayerEquipService playerEquipService;
    @Resource
    private ItemService itemService;
    @Resource
    private ItemWeaponService itemWeaponService;
    @Resource
    private ItemAccessoryService itemAccessoryService;

    public int calcRoleAction(String uid, PlayerRole role) {
        List<BigDecimal> ups = new ArrayList<>();
        List<BigDecimal> downs = new ArrayList<>();
        collectRole(role, ups, downs);
        if (!Wx.isEmpty(uid)) {
            collectEquip(uid, ups, downs);
        }
        return AtkSpeedCalcUnit.calcFinalAction(role == null ? null : role.getBaseAction(), ups, downs);
    }

    private void collectRole(PlayerRole role, List<BigDecimal> ups, List<BigDecimal> downs) {
        if (role == null) {
            return;
        }
        add(ups, role.getAtkSpeedUpRatio());
        add(downs, role.getAtkSpeedDownRatio());
    }

    private void collectEquip(String uid, List<BigDecimal> ups, List<BigDecimal> downs) {
        PlayerEquip equip = playerEquipService.getOrInit(uid);
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            if (Wx.isEmpty(itemId)) {
                continue;
            }
            collectItem(itemId, ups, downs);
        }
    }

    private void collectItem(String itemId, List<BigDecimal> ups, List<BigDecimal> downs) {
        Item item = itemService.getById(itemId);
        if (item == null || item.getItemType() == null) {
            return;
        }
        ItemType type = item.getItemType();
        switch (type) {
            case WEAPON -> {
                ItemWeapon ext = itemWeaponService.getByItemId(itemId);
                if (ext != null) {
                    add(ups, ext.getAtkSpeedUpRatio());
                    add(downs, ext.getAtkSpeedDownRatio());
                }
            }
            case ACCESSORY -> {
                ItemAccessory ext = itemAccessoryService.getByItemId(itemId);
                if (ext != null) {
                    add(ups, ext.getAtkSpeedUpRatio());
                    add(downs, ext.getAtkSpeedDownRatio());
                }
            }
            // 护甲 / 护手 / 头盔 / 护腿 不参与攻速
            default -> {
            }
        }
    }

    private void add(List<BigDecimal> list, BigDecimal v) {
        if (v != null && v.compareTo(BigDecimal.ZERO) != 0) {
            list.add(v);
        }
    }
}
