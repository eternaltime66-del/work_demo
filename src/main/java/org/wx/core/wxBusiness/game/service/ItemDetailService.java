package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemAccessory;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.entity.ItemMaterial;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;
import org.wx.core.wxBusiness.game.entity.vo.SkillLinkVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 组装物品详情（属性 / 技能等），供合成、装备槽等展示
 */
@Service
public class ItemDetailService {

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
    @Resource
    private ItemAccessoryService itemAccessoryService;
    @Resource
    private ItemMaterialService itemMaterialService;
    @Resource
    private ItemDefaultSkillService itemDefaultSkillService;
    @Resource
    private ActiveSkillService activeSkillService;

    public CraftItemDetailVo buildByItemId(String itemId) {
        if (Wx.isEmpty(itemId)) {
            return null;
        }
        return build(itemService.getById(itemId));
    }

    public CraftItemDetailVo build(Item item) {
        if (item == null) {
            return null;
        }
        CraftItemDetailVo d = new CraftItemDetailVo();
        d.setItemId(item.getId());
        d.setCode(item.getCode());
        d.setName(item.getName());
        d.setIcon(item.getIcon());
        d.setItemType(item.getItemType() != null ? item.getItemType().name() : null);
        d.setMaxStack(item.getMaxStack());
        d.setWeight(item.getWeight());
        d.setRemark(item.getRemark());
        d.setChargeSkillSlotCount(item.getChargeSkillSlotCount());
        d.setPlayerCanEditSkillSlot(item.getPlayerCanEditSkillSlot());

        ItemType type = item.getItemType();
        if (type == null) {
            return d;
        }
        switch (type) {
            case WEAPON -> {
                ItemWeapon ext = itemWeaponService.getByItemId(item.getId());
                if (ext != null) {
                    d.setBaseAtk(ext.getBaseAtk());
                    d.setAtkSpeedUpRatio(ext.getAtkSpeedUpRatio());
                    d.setAtkSpeedDownRatio(ext.getAtkSpeedDownRatio());
                    d.setNormalSkillId(ext.getNormalSkillId());
                    if (!Wx.isEmpty(ext.getNormalSkillId())) {
                        ActiveSkill skill = activeSkillService.getById(ext.getNormalSkillId());
                        if (skill != null) {
                            d.setNormalSkillName(skill.getName());
                        }
                    }
                }
            }
            case ARMOR -> fillArmorLike(d, itemArmorService.getByItemId(item.getId()));
            case GLOVES -> fillGloves(d, itemGlovesService.getByItemId(item.getId()));
            case HELMET -> fillHelmet(d, itemHelmetService.getByItemId(item.getId()));
            case LEGS -> fillLegs(d, itemLegsService.getByItemId(item.getId()));
            case ACCESSORY -> {
                ItemAccessory ext = itemAccessoryService.getByItemId(item.getId());
                if (ext != null) {
                    d.setAtkSpeedUpRatio(ext.getAtkSpeedUpRatio());
                    d.setAtkSpeedDownRatio(ext.getAtkSpeedDownRatio());
                }
            }
            case MATERIAL -> {
                ItemMaterial ext = itemMaterialService.getByItemId(item.getId());
                if (ext != null) {
                    d.setGrade(ext.getGrade());
                }
            }
            default -> {
            }
        }

        if (type != ItemType.MATERIAL) {
            List<ItemDefaultSkill> defaults = itemDefaultSkillService.listByItemId(item.getId());
            List<String> names = new ArrayList<>();
            List<SkillLinkVo> links = new ArrayList<>();
            for (ItemDefaultSkill row : defaults) {
                if (Wx.isEmpty(row.getSkillId())) {
                    continue;
                }
                String name = row.getSkillName() != null ? row.getSkillName() : row.getSkillId();
                names.add(name);
                links.add(SkillLinkVo.of(row.getSkillId(), name, row.getSkillType()));
            }
            d.setDefaultSkillNames(names);
            d.setDefaultSkills(links);
        }
        return d;
    }

    private void fillArmorLike(CraftItemDetailVo d, ItemArmor ext) {
        if (ext == null) {
            return;
        }
        d.setHp(ext.getHp());
        d.setDefense(ext.getDefense());
    }

    private void fillGloves(CraftItemDetailVo d, ItemGloves ext) {
        if (ext == null) {
            return;
        }
        d.setHp(ext.getHp());
        d.setDefense(ext.getDefense());
    }

    private void fillHelmet(CraftItemDetailVo d, ItemHelmet ext) {
        if (ext == null) {
            return;
        }
        d.setHp(ext.getHp());
        d.setDefense(ext.getDefense());
    }

    private void fillLegs(CraftItemDetailVo d, ItemLegs ext) {
        if (ext == null) {
            return;
        }
        d.setHp(ext.getHp());
        d.setDefense(ext.getDefense());
    }
}
