package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.BattleBag;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.mapper.BattleBagMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class BattleBagService extends WxServiceImpl<BattleBagMapper, BattleBag> {

    @Resource
    private ItemService itemService;

    public List<BattleBag> listByUid(String uid) {
        if (Wx.isEmpty(uid)) {
            return Collections.emptyList();
        }
        List<BattleBag> list = this.find()
                .eq(BattleBag::getUid, uid)
                .orderByAsc(BattleBag::getSort)
                .list();
        fillItemDetail(list);
        return list;
    }

    public BattleBag findByUidAndItemId(String uid, String itemId) {
        return this.find()
                .eq(BattleBag::getUid, uid)
                .eq(BattleBag::getItemId, itemId)
                .one();
    }

    /**
     * 向战斗背包添加物品（同 itemId 合并堆叠）
     */
    @Transactional(rollbackFor = Exception.class)
    public BattleBag addItem(String uid, String itemId, Integer qty) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        ErrorFactory.throwError(Wx.isEmpty(itemId), "物品不能为空");
        int addQty = qty == null ? 0 : qty;
        ErrorFactory.throwError(addQty <= 0, "数量必须大于0");
        Item item = itemService.getById(itemId);
        ErrorFactory.throwError(item == null, "物品不存在");

        BattleBag bag = findByUidAndItemId(uid, itemId);
        if (bag == null) {
            bag = new BattleBag();
            bag.setUid(uid);
            bag.setItemId(itemId);
            bag.setQuantity(addQty);
            bag.setSort(nextSort(uid));
            this.save(bag);
        } else {
            int cur = bag.getQuantity() == null ? 0 : bag.getQuantity();
            bag.setQuantity(cur + addQty);
            this.updateById(bag);
        }
        bag.setItemName(item.getName());
        bag.setIcon(item.getIcon());
        bag.setItemType(item.getItemType());
        bag.setMaxStack(item.getMaxStack());
        bag.setWeight(item.getWeight());
        return bag;
    }

    /**
     * 按 itemId 消耗数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(String uid, String itemId, int qty) {
        ErrorFactory.throwError(qty <= 0, "数量必须大于0");
        BattleBag bag = findByUidAndItemId(uid, itemId);
        ErrorFactory.notNull(bag, "背包中没有该物品");
        takeQuantity(bag, qty);
    }

    /**
     * 按背包行 id 取出数量，返回 itemId
     */
    @Transactional(rollbackFor = Exception.class)
    public String takeByBagId(String uid, String bagId, Integer quantity) {
        BattleBag bag = this.getById(bagId);
        ErrorFactory.notNull(bag, "背包物品不存在");
        ErrorFactory.notEquals(uid, bag.getUid(), "背包物品不匹配");
        int available = bag.getQuantity() == null ? 0 : bag.getQuantity();
        ErrorFactory.throwError(available <= 0, "背包物品数量不足");
        int take = quantity == null || quantity <= 0 ? available : Math.min(quantity, available);
        String itemId = bag.getItemId();
        takeQuantity(bag, take);
        return itemId;
    }

    private void takeQuantity(BattleBag bag, int qty) {
        int available = bag.getQuantity() == null ? 0 : bag.getQuantity();
        ErrorFactory.throwError(available < qty, "背包数量不足");
        int after = available - qty;
        if (after <= 0) {
            this.removeById(bag.getId());
        } else {
            bag.setQuantity(after);
            this.updateById(bag);
        }
    }

    private int nextSort(String uid) {
        List<BattleBag> rows = this.find().eq(BattleBag::getUid, uid).list();
        return rows.stream().map(BattleBag::getSort).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
    }

    public void fillItemDetail(List<BattleBag> list) {
        if (list == null) {
            return;
        }
        for (BattleBag row : list) {
            Item item = itemService.getById(row.getItemId());
            if (item != null) {
                row.setItemName(item.getName());
                row.setIcon(item.getIcon());
                row.setItemType(item.getItemType());
                row.setMaxStack(item.getMaxStack());
                row.setWeight(item.getWeight());
            }
        }
    }

    public void fillItemName(List<BattleBag> list) {
        fillItemDetail(list);
    }
}
