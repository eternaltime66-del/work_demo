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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ItemDefaultPassiveService extends WxServiceImpl<ItemDefaultPassiveMapper, ItemDefaultPassive> {

    @Resource
    private ItemService itemService;
    @Resource
    private PassiveSkillService passiveSkillService;

    public List<ItemDefaultPassive> listByItemId(String itemId, PassiveSkillType passiveType) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        ErrorFactory.notNull(passiveType, "被动类型不能为空");
        ErrorFactory.throwError(!isSupportedType(passiveType), "不支持的被动类型");
        List<ItemDefaultPassive> list = this.find()
                .eq(ItemDefaultPassive::getItemId, itemId)
                .eq(ItemDefaultPassive::getPassiveType, passiveType)
                .orderByAsc(ItemDefaultPassive::getSlotNo)
                .orderByAsc(ItemDefaultPassive::getSort)
                .list();
        for (ItemDefaultPassive row : list) {
            PassiveSkill skill = passiveSkillService.getById(row.getPassiveSkillId());
            if (skill != null) {
                row.setPassiveSkillName(skill.getName());
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveForItem(String itemId, PassiveSkillType passiveType, List<ItemDefaultPassive> passives) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        ErrorFactory.notNull(passiveType, "被动类型不能为空");
        ErrorFactory.throwError(!isSupportedType(passiveType), "不支持的被动类型");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");

        int slotCount = slotCountOf(item, passiveType);
        ErrorFactory.throwError(slotCount <= 0, "请先设置" + slotHint(passiveType));

        List<ItemDefaultPassive> rows = passives != null ? passives : List.of();
        ErrorFactory.throwError(rows.size() > slotCount, "被动槽数量超出装备设定");

        Set<String> used = new HashSet<>();
        List<ItemDefaultPassive> cleaned = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            ItemDefaultPassive row = i < rows.size() ? rows.get(i) : null;
            if (row == null || !StringUtils.hasText(row.getPassiveSkillId())) {
                continue;
            }
            ErrorFactory.throwError(!used.add(row.getPassiveSkillId()), "同一被动不能重复配置");
            PassiveSkill skill = passiveSkillService.getById(row.getPassiveSkillId());
            ErrorFactory.notNull(skill, "被动不存在");
            ErrorFactory.throwError(skill.getPassiveType() != passiveType, "被动类型与槽位不匹配");
            ItemDefaultPassive save = new ItemDefaultPassive();
            save.setItemId(itemId);
            save.setPassiveSkillId(row.getPassiveSkillId());
            save.setPassiveType(passiveType);
            save.setSlotNo(i);
            save.setSort(i);
            cleaned.add(save);
        }

        this.remove(new LambdaQueryWrapper<ItemDefaultPassive>()
                .eq(ItemDefaultPassive::getItemId, itemId)
                .eq(ItemDefaultPassive::getPassiveType, passiveType));
        for (ItemDefaultPassive save : cleaned) {
            this.save(save);
        }
    }

    private static boolean isSupportedType(PassiveSkillType type) {
        return type == PassiveSkillType.OUT_BASIC
                || type == PassiveSkillType.OUT_ADVANCED
                || type == PassiveSkillType.IN_ANCHOR
                || type == PassiveSkillType.IN_PERIODIC;
    }

    private static String slotHint(PassiveSkillType type) {
        return switch (type) {
            case OUT_BASIC -> "默认自带基础被动数量";
            case OUT_ADVANCED -> "默认自带高级属性被动数量";
            case IN_ANCHOR -> "默认自带锚点被动数量";
            case IN_PERIODIC -> "默认自带周期被动数量";
            default -> "被动槽数量";
        };
    }

    private static int slotCountOf(Item item, PassiveSkillType type) {
        Integer n = switch (type) {
            case OUT_BASIC -> item.getBasicPassiveSlotCount();
            case OUT_ADVANCED -> item.getAdvancedPassiveSlotCount();
            case IN_ANCHOR -> item.getAnchorPassiveSlotCount();
            case IN_PERIODIC -> item.getPeriodicPassiveSlotCount();
            default -> 0;
        };
        return n != null ? n : 0;
    }
}
