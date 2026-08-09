package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.battle.PassiveConditionEvalUnit;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.PassiveEffect;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveEffectAttrKey;
import org.wx.core.wxBusiness.game.entity.vo.EquipBonusVo;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 汇总玩家当前装备的攻击/生命/防御加成（扩展表平坦值 + 默认 OUT 被动）。
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
        applyOutPassives(uid, bonus);
        return bonus;
    }

    private void applyOutPassives(String uid, EquipBonusVo bonus) {
        List<PassiveSkill> outs = playerEquipService.resolveEquippedOutPassives(uid);
        if (outs == null || outs.isEmpty()) {
            return;
        }
        Set<String> equippedItemIds = playerEquipService.resolveEquippedItemIds(uid);
        Set<ItemType> equippedItemTypes = playerEquipService.resolveEquippedItemTypes(uid);
        Set<String> equippedSkillIds = new HashSet<>();
        Set<ActiveSkillType> equippedSkillTypes = new HashSet<>();
        ActiveSkill weaponNormal = playerEquipService.resolveWeaponNormalSkill(uid);
        if (weaponNormal != null && weaponNormal.getId() != null) {
            equippedSkillIds.add(weaponNormal.getId());
            if (weaponNormal.getSkillType() != null) {
                equippedSkillTypes.add(weaponNormal.getSkillType());
            }
        }
        List<ActiveSkill> chargeSkills = playerEquipService.resolveEquippedDefaultSkills(uid);
        if (chargeSkills != null) {
            for (ActiveSkill s : chargeSkills) {
                if (s == null || s.getId() == null) {
                    continue;
                }
                equippedSkillIds.add(s.getId());
                if (s.getSkillType() != null) {
                    equippedSkillTypes.add(s.getSkillType());
                }
            }
        }
        for (PassiveSkill p : outs) {
            if (!PassiveConditionEvalUnit.matchEquipConditions(
                    p, equippedItemIds, equippedSkillIds, equippedItemTypes, equippedSkillTypes)) {
                continue;
            }
            List<PassiveEffect> effects = p.getEffects();
            if (effects == null) {
                continue;
            }
            for (PassiveEffect e : effects) {
                applyOutEffect(bonus, e);
            }
        }
    }

    private void applyOutEffect(EquipBonusVo bonus, PassiveEffect e) {
        if (e == null || e.getAttrKey() == null || e.getValueNum() == null) {
            return;
        }
        BigDecimal signed = signedValue(e.getAttrDir(), e.getValueNum());
        PassiveEffectAttrKey key = e.getAttrKey();
        switch (key) {
            case ATK -> bonus.setAtk(bonus.getAtk() + signed.intValue());
            case MAX_HP -> bonus.setHp(bonus.getHp() + signed.intValue());
            case DEF -> bonus.setDefense(bonus.getDefense() + signed.intValue());
            case FINAL_ATK -> bonus.setFinalAtkRatioAdd(nvlBd(bonus.getFinalAtkRatioAdd()).add(signed));
            case FINAL_HP -> bonus.setFinalHpRatioAdd(nvlBd(bonus.getFinalHpRatioAdd()).add(signed));
            case FINAL_DEF -> bonus.setFinalDefRatioAdd(nvlBd(bonus.getFinalDefRatioAdd()).add(signed));
            case ATK_SPEED -> {
                if (signed.compareTo(BigDecimal.ZERO) >= 0) {
                    bonus.setAtkSpeedUpAdd(nvlBd(bonus.getAtkSpeedUpAdd()).add(signed));
                } else {
                    bonus.setAtkSpeedDownAdd(nvlBd(bonus.getAtkSpeedDownAdd()).add(signed.abs()));
                }
            }
            case DEAL_DMG_RATIO -> bonus.setDealDmgMult(mulRatio(bonus.getDealDmgMult(), signed));
            case TAKEN_DMG_RATIO -> bonus.setTakenDmgMult(mulRatio(bonus.getTakenDmgMult(), signed));
            case DEAL_ELEMENT_DMG_RATIO -> bonus.setDealElementDmgMult(mulRatio(bonus.getDealElementDmgMult(), signed));
            case TAKEN_ELEMENT_DMG_RATIO -> bonus.setTakenElementDmgMult(mulRatio(bonus.getTakenElementDmgMult(), signed));
            case DEAL_PHYS_DMG_RATIO -> bonus.setDealPhysDmgMult(mulRatio(bonus.getDealPhysDmgMult(), signed));
            case TAKEN_PHYS_DMG_RATIO -> bonus.setTakenPhysDmgMult(mulRatio(bonus.getTakenPhysDmgMult(), signed));
            default -> {
                // 吸血等暂不进展示面板
            }
        }
    }

    /** signed 为 ±p（单位 1%），按先后顺序叠乘到当前乘数 */
    private static double mulRatio(double current, BigDecimal signedPercent) {
        if (signedPercent == null || signedPercent.compareTo(BigDecimal.ZERO) == 0) {
            return current <= 0D ? 0.0001D : current;
        }
        double factor = 1D + signedPercent.doubleValue() / 100D;
        if (factor <= 0D) {
            factor = 0.0001D;
        }
        double next = current * factor;
        return next <= 0D ? 0.0001D : next;
    }

    private static BigDecimal signedValue(AttrModifyDirection dir, BigDecimal value) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value;
        if (dir == AttrModifyDirection.DECREASE) {
            return v.negate();
        }
        return v;
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

    private static BigDecimal nvlBd(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
