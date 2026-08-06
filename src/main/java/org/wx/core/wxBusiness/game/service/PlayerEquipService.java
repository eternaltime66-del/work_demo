package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemDefaultPassive;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;
import org.wx.core.wxBusiness.game.entity.vo.EquipSlotVo;
import org.wx.core.wxBusiness.game.mapper.PlayerEquipMapper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PlayerEquipService extends WxServiceImpl<PlayerEquipMapper, PlayerEquip> {

    @Resource
    private BattleBagService battleBagService;
    @Resource
    private ItemService itemService;
    @Resource
    private ItemWeaponService itemWeaponService;
    @Resource
    private ActiveSkillService activeSkillService;
    @Resource
    private ItemDetailService itemDetailService;
    @Resource
    private ItemDefaultSkillService itemDefaultSkillService;
    @Resource
    private ItemDefaultPassiveService itemDefaultPassiveService;
    @Resource
    private PassiveSkillService passiveSkillService;

    public PlayerEquip getOrInit(String uid) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        PlayerEquip row = this.find().eq(PlayerEquip::getUid, uid).one();
        if (row != null) {
            return row;
        }
        row = new PlayerEquip();
        row.setUid(uid);
        this.save(row);
        return row;
    }

    /**
     * 当前装备武器若配置了普攻槽，返回该普攻技能；
     * 未配置 / 已清空则返回 null（战斗侧回落通用默认普攻）。
     */
    public ActiveSkill resolveWeaponNormalSkill(String uid) {
        if (Wx.isEmpty(uid)) {
            return null;
        }
        PlayerEquip equip = this.find().eq(PlayerEquip::getUid, uid).one();
        if (equip == null || Wx.isEmpty(equip.getWeaponItemId())) {
            return null;
        }
        ItemWeapon weapon = itemWeaponService.getByItemId(equip.getWeaponItemId());
        if (weapon == null || Wx.isEmpty(weapon.getNormalSkillId())) {
            return null;
        }
        ActiveSkill skill = activeSkillService.getById(weapon.getNormalSkillId());
        if (skill == null || Boolean.FALSE.equals(skill.getEnable())) {
            return null;
        }
        if (skill.getSkillType() != ActiveSkillType.NORMAL) {
            return null;
        }
        return skill;
    }

    /**
     * 当前穿戴装备上的默认充能技能（大招/小技能），按技能 id 去重。
     * 卸下装备后不再带入战斗。
     */
    public List<ActiveSkill> resolveEquippedDefaultSkills(String uid) {
        List<ActiveSkill> list = new ArrayList<>();
        if (Wx.isEmpty(uid)) {
            return list;
        }
        PlayerEquip equip = this.find().eq(PlayerEquip::getUid, uid).one();
        if (equip == null) {
            return list;
        }
        Map<String, ActiveSkill> uniq = new LinkedHashMap<>();
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            if (Wx.isEmpty(itemId)) {
                continue;
            }
            List<ItemDefaultSkill> defaults = itemDefaultSkillService.listByItemId(itemId);
            if (defaults == null) {
                continue;
            }
            for (ItemDefaultSkill row : defaults) {
                if (row == null || Wx.isEmpty(row.getSkillId()) || uniq.containsKey(row.getSkillId())) {
                    continue;
                }
                ActiveSkill skill = activeSkillService.getById(row.getSkillId());
                if (skill == null || Boolean.FALSE.equals(skill.getEnable())) {
                    continue;
                }
                if (skill.getSkillType() == null || skill.getSkillType() == ActiveSkillType.NORMAL) {
                    continue;
                }
                uniq.put(skill.getId(), skill);
            }
        }
        list.addAll(uniq.values());
        return list;
    }

    /**
     * 当前穿戴装备上的默认锚点被动（去重），含完整条件与战斗效果。
     */
    public List<PassiveSkill> resolveEquippedAnchorPassives(String uid) {
        return resolveEquippedCombatPassives(uid, PassiveSkillType.IN_ANCHOR);
    }

    /**
     * 当前穿戴装备上的默认周期被动（去重），含完整条件与战斗效果。
     */
    public List<PassiveSkill> resolveEquippedPeriodicPassives(String uid) {
        return resolveEquippedCombatPassives(uid, PassiveSkillType.IN_PERIODIC);
    }

    /**
     * 当前穿戴装备上的默认持续效果被动（去重），含完整条件与战斗效果。
     */
    public List<PassiveSkill> resolveEquippedSustainedPassives(String uid) {
        return resolveEquippedCombatPassives(uid, PassiveSkillType.IN_SUSTAINED);
    }

    /**
     * 当前穿戴装备上的默认战斗外被动（基础+高级，去重），含效果与条件。
     */
    public List<PassiveSkill> resolveEquippedOutPassives(String uid) {
        List<PassiveSkill> list = new ArrayList<>();
        list.addAll(resolveEquippedCombatPassives(uid, PassiveSkillType.OUT_BASIC));
        list.addAll(resolveEquippedCombatPassives(uid, PassiveSkillType.OUT_ADVANCED));
        return list;
    }

    private List<PassiveSkill> resolveEquippedCombatPassives(String uid, PassiveSkillType type) {
        List<PassiveSkill> list = new ArrayList<>();
        if (Wx.isEmpty(uid) || type == null) {
            return list;
        }
        PlayerEquip equip = this.find().eq(PlayerEquip::getUid, uid).one();
        if (equip == null) {
            return list;
        }
        Map<String, PassiveSkill> uniq = new LinkedHashMap<>();
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            if (Wx.isEmpty(itemId)) {
                continue;
            }
            List<ItemDefaultPassive> defaults = itemDefaultPassiveService.listByItemId(itemId, type);
            if (defaults == null) {
                continue;
            }
            for (ItemDefaultPassive row : defaults) {
                if (row == null || Wx.isEmpty(row.getPassiveSkillId()) || uniq.containsKey(row.getPassiveSkillId())) {
                    continue;
                }
                PassiveSkill detail = passiveSkillService.getDetail(row.getPassiveSkillId());
                if (detail == null || Boolean.FALSE.equals(detail.getEnable())) {
                    continue;
                }
                if (detail.getPassiveType() != type) {
                    continue;
                }
                uniq.put(detail.getId(), detail);
            }
        }
        list.addAll(uniq.values());
        return list;
    }

    /** 当前已装备物品 id */
    public Set<String> resolveEquippedItemIds(String uid) {
        Set<String> ids = new LinkedHashSet<>();
        if (Wx.isEmpty(uid)) {
            return ids;
        }
        PlayerEquip equip = this.find().eq(PlayerEquip::getUid, uid).one();
        if (equip == null) {
            return ids;
        }
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            if (!Wx.isEmpty(itemId)) {
                ids.add(itemId);
            }
        }
        return ids;
    }

    /** 当前已装备物品类型 */
    public Set<ItemType> resolveEquippedItemTypes(String uid) {
        Set<ItemType> types = EnumSet.noneOf(ItemType.class);
        for (String itemId : resolveEquippedItemIds(uid)) {
            Item item = itemService.getById(itemId);
            if (item != null && item.getItemType() != null) {
                types.add(item.getItemType());
            }
        }
        return types;
    }

    public List<EquipSlotVo> buildSlotOverview(PlayerEquip equip) {
        List<EquipSlotVo> list = new ArrayList<>();
        for (EquipSlot slot : EquipSlot.values()) {
            String itemId = slot.getItemId(equip);
            String name = null;
            String icon = null;
            CraftItemDetailVo detail = null;
            if (!Wx.isEmpty(itemId)) {
                Item item = itemService.getById(itemId);
                if (item != null) {
                    name = item.getName();
                    icon = item.getIcon();
                    detail = itemDetailService.buildRich(item);
                }
            }
            EquipSlotVo vo = EquipSlotVo.of(slot, itemId, name, icon);
            vo.setDetail(detail);
            list.add(vo);
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerEquip equipSlot(String uid, EquipSlot slot, String itemId) {
        ErrorFactory.notNull(slot, "请选择装备槽位");
        ErrorFactory.notEmpty(itemId, "请选择装备");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");
        ErrorFactory.throwError(item.getItemType() != slot.getRequiredType(),
                "该物品不能装备到" + slot.getLabel());

        PlayerEquip equip = getOrInit(uid);
        assertNotEquippedElsewhere(equip, slot, itemId);

        String currentItemId = slot.getItemId(equip);
        if (itemId.equals(currentItemId)) {
            return equip;
        }
        if (!Wx.isEmpty(currentItemId)) {
            unequipSlotInternal(uid, equip, slot);
        }

        battleBagService.consume(uid, itemId, 1);
        persistSlotItemId(equip, slot, itemId);
        return equip;
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerEquip unequipSlot(String uid, EquipSlot slot) {
        ErrorFactory.notNull(slot, "请选择装备槽位");
        PlayerEquip equip = getOrInit(uid);
        unequipSlotInternal(uid, equip, slot);
        persistSlotItemId(equip, slot, null);
        return equip;
    }

    private void unequipSlotInternal(String uid, PlayerEquip equip, EquipSlot slot) {
        String itemId = slot.getItemId(equip);
        if (Wx.isEmpty(itemId)) {
            return;
        }
        slot.setItemId(equip, null);
        battleBagService.addItem(uid, itemId, 1);
    }

    private void persistSlotItemId(PlayerEquip equip, EquipSlot slot, String itemId) {
        slot.setItemId(equip, itemId);
        LambdaUpdateWrapper<PlayerEquip> wrapper = new LambdaUpdateWrapper<PlayerEquip>()
                .eq(PlayerEquip::getId, equip.getId());
        switch (slot) {
            case WEAPON -> wrapper.set(PlayerEquip::getWeaponItemId, itemId);
            case ARMOR -> wrapper.set(PlayerEquip::getArmorItemId, itemId);
            case GLOVES -> wrapper.set(PlayerEquip::getGlovesItemId, itemId);
            case HELMET -> wrapper.set(PlayerEquip::getHelmetItemId, itemId);
            case LEGS -> wrapper.set(PlayerEquip::getLegsItemId, itemId);
            case ACCESSORY_1 -> wrapper.set(PlayerEquip::getAccessory1ItemId, itemId);
            case ACCESSORY_2 -> wrapper.set(PlayerEquip::getAccessory2ItemId, itemId);
            case ACCESSORY_3 -> wrapper.set(PlayerEquip::getAccessory3ItemId, itemId);
        }
        this.update(wrapper);
    }

    private void assertNotEquippedElsewhere(PlayerEquip equip, EquipSlot targetSlot, String itemId) {
        for (EquipSlot slot : EquipSlot.values()) {
            if (slot == targetSlot) {
                continue;
            }
            if (itemId.equals(slot.getItemId(equip))) {
                ErrorFactory.throwError(true, "该物品已在" + slot.getLabel() + "装备");
            }
        }
    }
}
