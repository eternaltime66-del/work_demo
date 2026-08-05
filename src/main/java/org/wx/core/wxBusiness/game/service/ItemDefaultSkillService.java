package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.mapper.ItemDefaultSkillMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ItemDefaultSkillService extends WxServiceImpl<ItemDefaultSkillMapper, ItemDefaultSkill> {

    @Resource
    private ItemService itemService;
    @Resource
    private ActiveSkillService activeSkillService;

    public List<ItemDefaultSkill> listByItemId(String itemId) {
        List<ItemDefaultSkill> list = this.find()
                .eq(ItemDefaultSkill::getItemId, itemId)
                .orderByAsc(ItemDefaultSkill::getSlotNo)
                .orderByAsc(ItemDefaultSkill::getSort)
                .list();
        for (ItemDefaultSkill row : list) {
            ActiveSkill skill = activeSkillService.getById(row.getSkillId());
            if (skill != null) {
                row.setSkillName(skill.getName());
                row.setSkillType(skill.getSkillType() != null ? skill.getSkillType().name() : null);
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveForItem(String itemId, List<ItemDefaultSkill> skills) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");

        int slotCount = item.getChargeSkillSlotCount() != null ? item.getChargeSkillSlotCount() : 0;
        ErrorFactory.throwError(slotCount <= 0, "请先设置充能技能槽数量");

        List<ItemDefaultSkill> rows = skills != null ? skills : List.of();
        // 允许空槽：未配置的槽不落库；有配置的槽按序号保存
        ErrorFactory.throwError(rows.size() > slotCount, "技能槽数量超出装备设定");

        Set<String> used = new HashSet<>();
        List<ItemDefaultSkill> cleaned = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            ItemDefaultSkill row = i < rows.size() ? rows.get(i) : null;
            if (row == null || !StringUtils.hasText(row.getSkillId())) {
                continue;
            }
            ErrorFactory.throwError(!used.add(row.getSkillId()), "同一技能不能重复配置");
            ActiveSkill skill = activeSkillService.getById(row.getSkillId());
            ErrorFactory.notNull(skill, "技能不存在");
            ErrorFactory.throwError(skill.getSkillType() == null || skill.getSkillType() == ActiveSkillType.NORMAL,
                    "技能槽只能配置大招/小技能");
            ItemDefaultSkill save = new ItemDefaultSkill();
            save.setItemId(itemId);
            save.setSkillId(row.getSkillId());
            save.setSlotNo(i);
            save.setSort(i);
            cleaned.add(save);
        }

        this.remove(new LambdaQueryWrapper<ItemDefaultSkill>().eq(ItemDefaultSkill::getItemId, itemId));
        for (ItemDefaultSkill save : cleaned) {
            this.save(save);
        }
    }
}
