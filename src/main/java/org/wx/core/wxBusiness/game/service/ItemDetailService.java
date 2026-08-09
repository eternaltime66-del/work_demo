package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.battle.GameDescUnit;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.entity.ItemDefaultPassive;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.entity.ItemMaterial;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.enums.ItemType;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;
import org.wx.core.wxBusiness.game.entity.vo.PassiveDescVo;
import org.wx.core.wxBusiness.game.entity.vo.SkillDescVo;
import org.wx.core.wxBusiness.game.entity.vo.SkillLinkVo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 组装物品详情（属性 / 技能等），供合成、装备槽、后台详情展示
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
    private ItemMaterialService itemMaterialService;
    @Resource
    private ItemDefaultSkillService itemDefaultSkillService;
    @Resource
    private ItemDefaultPassiveService itemDefaultPassiveService;
    @Resource
    private ActiveSkillService activeSkillService;
    @Resource
    private SkillChargeService skillChargeService;
    @Resource
    private SkillEffectService skillEffectService;
    @Resource
    private SkillOutputService skillOutputService;
    @Resource
    private PassiveSkillService passiveSkillService;
    @Resource
    private BuffDefService buffDefService;

    public CraftItemDetailVo buildByItemId(String itemId) {
        if (Wx.isEmpty(itemId)) {
            return null;
        }
        return build(itemService.getById(itemId));
    }

    /** 含技能/被动人性化文案（玩家装备详情 / 后台详情） */
    public CraftItemDetailVo buildRichByItemId(String itemId) {
        CraftItemDetailVo d = buildByItemId(itemId);
        if (d == null) {
            return null;
        }
        fillRichDescs(d);
        return d;
    }

    public CraftItemDetailVo buildRich(Item item) {
        CraftItemDetailVo d = build(item);
        if (d == null) {
            return null;
        }
        fillRichDescs(d);
        return d;
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
        d.setItemTypeLabel(item.getItemType() != null ? item.getItemType().label() : null);
        d.setMaxStack(item.getMaxStack());
        d.setRemark(item.getRemark());
        d.setChargeSkillSlotCount(item.getChargeSkillSlotCount());
        d.setPlayerDefaultEditChargeSkillSlotCount(item.getPlayerDefaultEditChargeSkillSlotCount());
        d.setPlayerMaxEditChargeSkillSlotCount(item.getPlayerMaxEditChargeSkillSlotCount());
        d.setBasicPassiveSlotCount(item.getBasicPassiveSlotCount());
        d.setPlayerDefaultEditBasicPassiveSlotCount(item.getPlayerDefaultEditBasicPassiveSlotCount());
        d.setPlayerMaxEditBasicPassiveSlotCount(item.getPlayerMaxEditBasicPassiveSlotCount());
        d.setAdvancedPassiveSlotCount(item.getAdvancedPassiveSlotCount());
        d.setPlayerDefaultEditAdvancedPassiveSlotCount(item.getPlayerDefaultEditAdvancedPassiveSlotCount());
        d.setPlayerMaxEditAdvancedPassiveSlotCount(item.getPlayerMaxEditAdvancedPassiveSlotCount());

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
                    d.setAtkSpeedText(GameDescUnit.atkSpeedText(ext.getAtkSpeedUpRatio(), ext.getAtkSpeedDownRatio()));
                    if (!Wx.isEmpty(ext.getNormalSkillId())) {
                        ActiveSkill skill = activeSkillService.getById(ext.getNormalSkillId());
                        if (skill != null) {
                            d.setNormalSkillId(ext.getNormalSkillId());
                            d.setNormalSkillName(skill.getName());
                            d.setNormalSkillSystemDefault(false);
                        }
                    }
                    // 未绑定或绑定已失效 → 回落系统默认普攻（与战斗一致）
                    if (Wx.isEmpty(d.getNormalSkillId())) {
                        fillSystemDefaultNormal(d);
                    }
                }
            }
            case ARMOR -> fillArmorLike(d, itemArmorService.getByItemId(item.getId()));
            case GLOVES -> fillGloves(d, itemGlovesService.getByItemId(item.getId()));
            case HELMET -> fillHelmet(d, itemHelmetService.getByItemId(item.getId()));
            case LEGS -> fillLegs(d, itemLegsService.getByItemId(item.getId()));
            case ACCESSORY -> {
            }
            case SKILL_STONE -> {
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

    private void fillRichDescs(CraftItemDetailVo d) {
        Function<String, String> skillNameFn = id -> {
            ActiveSkill sk = activeSkillService.getById(id);
            return sk != null ? sk.getName() : null;
        };
        Function<String, String> itemNameFn = id -> {
            Item it = itemService.getById(id);
            return it != null ? it.getName() : null;
        };
        Function<String, BuffDef> buffFn = id -> Wx.isEmpty(id) ? null : buffDefService.getById(id);

        if (Wx.isEmpty(d.getNormalSkillId()) && "WEAPON".equals(d.getItemType())) {
            fillSystemDefaultNormal(d);
        }
        if (!Wx.isEmpty(d.getNormalSkillId())) {
            d.setNormalSkillDesc(buildSkillDesc(d.getNormalSkillId(), skillNameFn, buffFn));
        }

        List<SkillDescVo> skillDescs = new ArrayList<>();
        if (d.getDefaultSkills() != null) {
            for (SkillLinkVo link : d.getDefaultSkills()) {
                if (link == null || Wx.isEmpty(link.getId())) {
                    continue;
                }
                SkillDescVo desc = buildSkillDesc(link.getId(), skillNameFn, buffFn);
                if (desc != null && desc.getId() != null) {
                    skillDescs.add(desc);
                }
            }
        }
        d.setDefaultSkillDescs(skillDescs);

        d.setDefaultBasicPassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.OUT_BASIC, itemNameFn, skillNameFn, buffFn));
        d.setDefaultAdvancedPassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.OUT_ADVANCED, itemNameFn, skillNameFn, buffFn));
        d.setDefaultBattleStartPassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.BATTLE_START, itemNameFn, skillNameFn, buffFn));
        d.setDefaultBattleJudgePassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.BATTLE_JUDGE, itemNameFn, skillNameFn, buffFn));
        d.setDefaultBattlePulsePassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.BATTLE_PULSE, itemNameFn, skillNameFn, buffFn));
        d.setDefaultBattleCombatPassiveDescs(buildPassiveDescs(d.getItemId(), PassiveSkillType.BATTLE_COMBAT, itemNameFn, skillNameFn, buffFn));
    }

    private void fillSystemDefaultNormal(CraftItemDetailVo d) {
        if (d == null) {
            return;
        }
        ActiveSkill def = activeSkillService.ensureDefaultNormalSkill();
        if (def == null) {
            return;
        }
        d.setNormalSkillId(def.getId());
        d.setNormalSkillName(def.getName());
        d.setNormalSkillSystemDefault(true);
    }

    private SkillDescVo buildSkillDesc(
            String skillId,
            Function<String, String> skillNameFn,
            Function<String, BuffDef> buffFn
    ) {
        ActiveSkill skill = activeSkillService.getById(skillId);
        if (skill == null) {
            return null;
        }
        List<SkillCharge> charges = skillChargeService.listBySkillId(skillId);
        List<SkillOutput> outputs = skillOutputService.listBySkillId(skillId);
        List<SkillEffect> effects = (outputs == null || outputs.isEmpty())
                ? skillEffectService.listBySkillId(skillId)
                : List.of();
        return GameDescUnit.describeActiveSkill(skill, charges, effects, outputs, skillNameFn, buffFn);
    }

    private List<PassiveDescVo> buildPassiveDescs(
            String itemId,
            PassiveSkillType type,
            Function<String, String> itemNameFn,
            Function<String, String> skillNameFn,
            Function<String, BuffDef> buffFn
    ) {
        List<PassiveDescVo> list = new ArrayList<>();
        if (Wx.isEmpty(itemId)) {
            return list;
        }
        List<ItemDefaultPassive> rows = itemDefaultPassiveService.listByItemId(itemId, type);
        if (rows == null) {
            return list;
        }
        for (ItemDefaultPassive row : rows) {
            if (row == null || Wx.isEmpty(row.getPassiveSkillId())) {
                continue;
            }
            try {
                PassiveSkill detail = passiveSkillService.getDetail(row.getPassiveSkillId());
                if (detail == null) {
                    continue;
                }
                list.add(GameDescUnit.describePassive(detail, itemNameFn, skillNameFn, buffFn));
            } catch (Exception ignored) {
                // 单条被动描述失败不影响整件装备详情
            }
        }
        return list;
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
