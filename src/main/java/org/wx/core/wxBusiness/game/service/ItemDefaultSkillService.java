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

    /**
     * 按提交列表整表替换；未选技能的行自动丢弃，允许清空。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveForItem(String itemId, List<ItemDefaultSkill> skills) {
        ErrorFactory.notEmpty(itemId, "物品不能为空");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");

        Set<String> used = new HashSet<>();
        List<ItemDefaultSkill> cleaned = new ArrayList<>();
        int slot = 0;
        if (skills != null) {
            for (ItemDefaultSkill row : skills) {
                if (row == null || !StringUtils.hasText(row.getSkillId())) {
                    continue;
                }
                ErrorFactory.throwError(!used.add(row.getSkillId()), "同一技能不能重复配置");
                ActiveSkill skill = activeSkillService.getById(row.getSkillId());
                ErrorFactory.notNull(skill, "技能不存在");
                ErrorFactory.throwError(skill.getSkillType() == null || skill.getSkillType() == ActiveSkillType.NORMAL,
                        "只能配置大招/小技能");
                ItemDefaultSkill save = new ItemDefaultSkill();
                save.setItemId(itemId);
                save.setSkillId(row.getSkillId());
                save.setSlotNo(slot);
                save.setSort(slot);
                cleaned.add(save);
                slot++;
            }
        }

        this.remove(new LambdaQueryWrapper<ItemDefaultSkill>().eq(ItemDefaultSkill::getItemId, itemId));
        for (ItemDefaultSkill save : cleaned) {
            this.save(save);
        }
    }
}
