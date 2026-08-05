package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.Warehouse;
import org.wx.core.wxBusiness.game.entity.WarehouseItem;
import org.wx.core.wxBusiness.game.entity.vo.WarehouseVo;
import org.wx.core.wxBusiness.game.mapper.WarehouseMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WarehouseService extends WxServiceImpl<WarehouseMapper, Warehouse> {

    @Resource
    private WarehouseItemService warehouseItemService;
    @Resource
    private ItemService itemService;

    /**
     * 确保玩家有仓库，没有则创建（默认 100 格）
     */
    @Transactional(rollbackFor = Exception.class)
    public Warehouse ensureWarehouse(String uid) {
        if (Wx.isEmpty(uid)) {
            return null;
        }
        Warehouse wh = this.find().eq(Warehouse::getUid, uid).one();
        if (wh != null) {
            return wh;
        }
        wh = new Warehouse();
        wh.setUid(uid);
        wh.setMaxSlots(100);
        this.save(wh);
        return wh;
    }

    public WarehouseVo getWarehouseDetail(String uid) {
        Warehouse wh = ensureWarehouse(uid);
        List<WarehouseItem> slots = listItems(uid);
        WarehouseVo vo = new WarehouseVo();
        vo.setWarehouseId(wh.getId());
        vo.setMaxSlots(wh.getMaxSlots() == null ? 100 : wh.getMaxSlots());
        vo.setUsedSlots(slots.size());
        vo.setSlots(slots);
        return vo;
    }

    /**
     * 列出仓库物品，并填充物品详情
     */
    public List<WarehouseItem> listItems(String uid) {
        if (Wx.isEmpty(uid)) {
            return Collections.emptyList();
        }
        ensureWarehouse(uid);
        List<WarehouseItem> list = warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .orderByAsc(WarehouseItem::getSlotNo)
                .list();
        fillItemDetail(list);
        return list;
    }

    public WarehouseItem findSlot(String uid, Integer slotNo) {
        ErrorFactory.notNull(slotNo, "格子号不能为空");
        return warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .eq(WarehouseItem::getSlotNo, slotNo)
                .one();
    }

    /** 仓库内各物品数量合计 */
    public Map<String, Integer> countItems(String uid) {
        Map<String, Integer> map = new HashMap<>();
        if (Wx.isEmpty(uid)) {
            return map;
        }
        List<WarehouseItem> list = warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .list();
        for (WarehouseItem row : list) {
            if (row == null || Wx.isEmpty(row.getItemId())) {
                continue;
            }
            int qty = row.getQuantity() == null ? 0 : row.getQuantity();
            map.merge(row.getItemId(), qty, Integer::sum);
        }
        return map;
    }

    /**
     * 按物品 id 消耗仓库数量（多格依次扣）
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeItem(String uid, String itemId, int qty) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        ErrorFactory.throwError(Wx.isEmpty(itemId), "物品不能为空");
        ErrorFactory.throwError(qty <= 0, "数量必须大于0");
        List<WarehouseItem> same = warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .eq(WarehouseItem::getItemId, itemId)
                .orderByAsc(WarehouseItem::getSlotNo)
                .list();
        int owned = 0;
        for (WarehouseItem row : same) {
            owned += row.getQuantity() == null ? 0 : row.getQuantity();
        }
        ErrorFactory.throwError(owned < qty, "仓库材料不足");
        int remain = qty;
        for (WarehouseItem row : same) {
            if (remain <= 0) {
                break;
            }
            int cur = row.getQuantity() == null ? 0 : row.getQuantity();
            if (cur <= 0) {
                continue;
            }
            int take = Math.min(cur, remain);
            int after = cur - take;
            if (after <= 0) {
                warehouseItemService.removeById(row.getId());
            } else {
                row.setQuantity(after);
                warehouseItemService.updateById(row);
            }
            remain -= take;
        }
    }

    /**
     * 向仓库添加物品（优先堆叠，再占空格）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addItem(String uid, String itemId, int qty) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        ErrorFactory.throwError(Wx.isEmpty(itemId), "物品不能为空");
        ErrorFactory.throwError(qty <= 0, "数量必须大于0");
        Item item = itemService.getById(itemId);
        ErrorFactory.notNull(item, "物品不存在");
        int maxStack = item.getMaxStack() == null || item.getMaxStack() < 1 ? 99 : item.getMaxStack();
        Warehouse wh = ensureWarehouse(uid);
        int maxSlots = wh.getMaxSlots() == null ? 100 : wh.getMaxSlots();

        int remain = qty;
        List<WarehouseItem> same = warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .eq(WarehouseItem::getItemId, itemId)
                .orderByAsc(WarehouseItem::getSlotNo)
                .list();
        for (WarehouseItem row : same) {
            if (remain <= 0) {
                break;
            }
            int cur = row.getQuantity() == null ? 0 : row.getQuantity();
            int room = maxStack - cur;
            if (room <= 0) {
                continue;
            }
            int add = Math.min(room, remain);
            row.setQuantity(cur + add);
            warehouseItemService.updateById(row);
            remain -= add;
        }

        while (remain > 0) {
            Integer empty = nextEmptySlot(uid, maxSlots);
            ErrorFactory.throwError(empty == null, "仓库已满");
            int add = Math.min(maxStack, remain);
            WarehouseItem row = new WarehouseItem();
            row.setUid(uid);
            row.setWarehouseId(wh.getId());
            row.setItemId(itemId);
            row.setQuantity(add);
            row.setSlotNo(empty);
            warehouseItemService.save(row);
            remain -= add;
        }
    }

    /**
     * 从指定格子取出数量，返回取出的 itemId
     */
    @Transactional(rollbackFor = Exception.class)
    public String takeFromSlot(String uid, Integer slotNo, Integer quantity) {
        WarehouseItem row = findSlot(uid, slotNo);
        ErrorFactory.notNull(row, "仓库格子为空");
        int available = row.getQuantity() == null ? 0 : row.getQuantity();
        ErrorFactory.throwError(available <= 0, "仓库格子为空");
        int take = quantity == null || quantity <= 0 ? available : Math.min(quantity, available);
        int after = available - take;
        if (after <= 0) {
            warehouseItemService.removeById(row.getId());
        } else {
            row.setQuantity(after);
            warehouseItemService.updateById(row);
        }
        return row.getItemId();
    }

    private Integer nextEmptySlot(String uid, int maxSlots) {
        List<WarehouseItem> list = warehouseItemService.find()
                .eq(WarehouseItem::getUid, uid)
                .list();
        Set<Integer> used = new HashSet<>();
        for (WarehouseItem row : list) {
            if (row.getSlotNo() != null) {
                used.add(row.getSlotNo());
            }
        }
        for (int i = 1; i <= maxSlots; i++) {
            if (!used.contains(i)) {
                return i;
            }
        }
        return null;
    }

    public void fillItemDetail(List<WarehouseItem> list) {
        if (list == null) {
            return;
        }
        for (WarehouseItem row : list) {
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

    /** @deprecated use fillItemDetail */
    public void fillItemName(List<WarehouseItem> list) {
        fillItemDetail(list);
    }
}
