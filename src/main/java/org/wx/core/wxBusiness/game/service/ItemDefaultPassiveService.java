package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemDefaultPassive;
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;
import org.wx.core.wxBusiness.game.mapper.ItemDefaultPassiveMapper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ItemDefaultPassiveService extends WxServiceImpl<ItemDefaultPassiveMapper, ItemDefaultPassive> {

    @Resource
    private ItemService itemService;
    @Resource
    private PassiveSkillService passiveSkillService;

    public List<ItemDefaultPassive> listByItemId(String itemId, PassiveSkillType passiveType) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        if (passiveType == null) {
            return listAllByItemId(itemId);
        }
        ErrorFactory.throwError(!isSupportedType(passiveType), "不支持的被动类型");
        List<ItemDefaultPassive> list = this.find()
                .eq(ItemDefaultPassive::getItemId, itemId)
                .eq(ItemDefaultPassive::getPassiveType, passiveType)
                .orderByAsc(ItemDefaultPassive::getSlotNo)
                .orderByAsc(ItemDefaultPassive::getSort)
                .list();
        fillNames(list);
        return list;
    }

    public List<ItemDefaultPassive> listAllByItemId(String itemId) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        List<ItemDefaultPassive> list = this.find()
                .eq(ItemDefaultPassive::getItemId, itemId)
                .orderByAsc(ItemDefaultPassive::getPassiveType)
                .orderByAsc(ItemDefaultPassive::getSlotNo)
                .orderByAsc(ItemDefaultPassive::getSort)
                .list();
        fillNames(list);
        return list;
    }

    /**
     * 按类型整表替换；未选技能的行自动丢弃。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveForItem(String itemId, PassiveSkillType passiveType, List<ItemDefaultPassive> passives) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        ErrorFactory.notNull(passiveType, "被动类型不能为空");
        ErrorFactory.throwError(!isSupportedType(passiveType), "不支持的被动类型");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");

        List<ItemDefaultPassive> cleaned = cleanRows(itemId, passiveType, passives);
        this.remove(new LambdaQueryWrapper<ItemDefaultPassive>()
                .eq(ItemDefaultPassive::getItemId, itemId)
                .eq(ItemDefaultPassive::getPassiveType, passiveType));
        for (ItemDefaultPassive save : cleaned) {
            this.save(save);
        }
    }

    /**
     * 一次保存装备全部默认被动（混合类型）；整表替换该装备下所有默认被动。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAllForItem(String itemId, List<ItemDefaultPassive> passives) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");

        Map<PassiveSkillType, List<ItemDefaultPassive>> byType = new EnumMap<>(PassiveSkillType.class);
        Set<String> used = new HashSet<>();
        if (passives != null) {
            for (ItemDefaultPassive row : passives) {
                if (row == null || !StringUtils.hasText(row.getPassiveSkillId())) {
                    continue;
                }
                ErrorFactory.throwError(!used.add(row.getPassiveSkillId()), "同一被动不能重复配置");
                PassiveSkill skill = passiveSkillService.getById(row.getPassiveSkillId());
                ErrorFactory.notNull(skill, "被动不存在");
                PassiveSkillType type = row.getPassiveType() != null ? row.getPassiveType() : skill.getPassiveType();
                ErrorFactory.notNull(type, "被动类型不能为空");
                ErrorFactory.throwError(!isSupportedType(type), "不支持的被动类型");
                ErrorFactory.throwError(skill.getPassiveType() != type, "被动类型与所选类型不匹配");
                byType.computeIfAbsent(type, k -> new ArrayList<>()).add(row);
            }
        }

        this.remove(new LambdaQueryWrapper<ItemDefaultPassive>().eq(ItemDefaultPassive::getItemId, itemId));
        for (Map.Entry<PassiveSkillType, List<ItemDefaultPassive>> e : byType.entrySet()) {
            List<ItemDefaultPassive> cleaned = cleanRows(itemId, e.getKey(), e.getValue());
            for (ItemDefaultPassive save : cleaned) {
                this.save(save);
            }
        }
    }

    private List<ItemDefaultPassive> cleanRows(String itemId, PassiveSkillType passiveType, List<ItemDefaultPassive> passives) {
        Set<String> used = new HashSet<>();
        List<ItemDefaultPassive> cleaned = new ArrayList<>();
        int slot = 0;
        if (passives != null) {
            for (ItemDefaultPassive row : passives) {
                if (row == null || !StringUtils.hasText(row.getPassiveSkillId())) {
                    continue;
                }
                ErrorFactory.throwError(!used.add(row.getPassiveSkillId()), "同一被动不能重复配置");
                PassiveSkill skill = passiveSkillService.getById(row.getPassiveSkillId());
                ErrorFactory.notNull(skill, "被动不存在");
                ErrorFactory.throwError(skill.getPassiveType() != passiveType, "被动类型与所选类型不匹配");
                ItemDefaultPassive save = new ItemDefaultPassive();
                save.setItemId(itemId);
                save.setPassiveSkillId(row.getPassiveSkillId());
                save.setPassiveType(passiveType);
                save.setSlotNo(slot);
                save.setSort(slot);
                cleaned.add(save);
                slot++;
            }
        }
        return cleaned;
    }

    private void fillNames(List<ItemDefaultPassive> list) {
        if (list == null) {
            return;
        }
        for (ItemDefaultPassive row : list) {
            PassiveSkill skill = passiveSkillService.getById(row.getPassiveSkillId());
            if (skill != null) {
                row.setPassiveSkillName(skill.getName());
            }
        }
    }

    private static boolean isSupportedType(PassiveSkillType type) {
        return type != null && (type.isOutOfCombat() || type.isInCombat());
    }
}
