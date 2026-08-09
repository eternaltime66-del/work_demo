package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.MonsterDrop;
import org.wx.core.wxBusiness.game.entity.vo.MonsterDropResultVo;
import org.wx.core.wxBusiness.game.entity.vo.StageDropPreviewVo;
import org.wx.core.wxBusiness.game.mapper.MonsterDropMapper;
import org.wx.core.wxBusiness.game.unit.DropRandomUnit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MonsterDropService extends WxServiceImpl<MonsterDropMapper, MonsterDrop> {

    @Resource
    private ItemService itemService;
    @Resource
    private WarehouseService warehouseService;

    public List<MonsterDrop> listByMonsterId(String monsterId) {
        List<MonsterDrop> list = this.find()
                .eq(MonsterDrop::getMonsterId, monsterId)
                .orderByAsc(MonsterDrop::getSort)
                .list();
        fillItemName(list);
        return list;
    }

    /** 后台保存前统一校验，禁止模糊概率和隐藏的零数量掉落。 */
    public void savePrepared(MonsterDrop entity) {
        ErrorFactory.throwError(entity == null, "掉落配置不能为空");
        ErrorFactory.throwError(Wx.isEmpty(entity.getMonsterId()), "怪物不能为空");
        ErrorFactory.throwError(Wx.isEmpty(entity.getItemId()), "物品不能为空");
        BigDecimal rate = entity.getDropRate() == null ? BigDecimal.ZERO : entity.getDropRate();
        ErrorFactory.throwError(rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0,
                "掉落概率必须在0到100之间");
        int min = entity.getMinQty() == null ? 1 : entity.getMinQty();
        int max = entity.getMaxQty() == null ? min : entity.getMaxQty();
        ErrorFactory.throwError(min < 1, "最小掉落数量必须大于0");
        ErrorFactory.throwError(max < min, "最大掉落数量不能小于最小数量");
        ErrorFactory.throwError(max > 100000, "单次掉落数量不能超过100000");
        entity.setDropRate(rate);
        entity.setMinQty(min);
        entity.setMaxQty(max);
        entity.setSort(entity.getSort() == null ? 0 : entity.getSort());
        entity.setEnable(entity.getEnable() == null ? Boolean.TRUE : entity.getEnable());
        this.saveOrUpdate(entity);
    }

    public List<MonsterDrop> listEnabledByMonsterId(String monsterId) {
        if (Wx.isEmpty(monsterId)) {
            return Collections.emptyList();
        }
        List<MonsterDrop> list = this.find()
                .eq(MonsterDrop::getMonsterId, monsterId)
                .eq(MonsterDrop::getEnable, true)
                .orderByAsc(MonsterDrop::getSort)
                .list();
        fillItemName(list);
        return list;
    }

    /**
     * 击杀掉落结算：每种配置独立判定概率，命中后再按非线性公式抽数量。
     * dropRate：100 = 100%，互不影响。
     */
    public List<MonsterDropResultVo> rollDrops(String monsterId) {
        List<MonsterDrop> configs = listEnabledByMonsterId(monsterId);
        List<MonsterDropResultVo> results = new ArrayList<>();
        for (MonsterDrop config : configs) {
            if (!rollRate(config.getDropRate())) {
                continue;
            }
            int qty = rollQuantity(config.getMinQty(), config.getMaxQty());
            if (qty <= 0) {
                continue;
            }
            Item item = itemService.getById(config.getItemId());
            if (item == null || Boolean.FALSE.equals(item.getEnable())) {
                continue;
            }
            MonsterDropResultVo vo = new MonsterDropResultVo();
            vo.setMonsterId(monsterId);
            vo.setItemId(item.getId());
            vo.setItemName(item.getName());
            vo.setIcon(item.getIcon());
            vo.setItemType(item.getItemType());
            vo.setQuantity(qty);
            results.add(vo);
        }
        return results;
    }

    /**
     * 多个怪物各自独立掉落后，按 itemId 合并数量
     */
    public List<MonsterDropResultVo> rollDrops(Collection<String> monsterIds) {
        if (monsterIds == null || monsterIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, MonsterDropResultVo> merged = new LinkedHashMap<>();
        for (String monsterId : monsterIds) {
            if (Wx.isEmpty(monsterId)) {
                continue;
            }
            for (MonsterDropResultVo drop : rollDrops(monsterId)) {
                MonsterDropResultVo exist = merged.get(drop.getItemId());
                if (exist == null) {
                    merged.put(drop.getItemId(), drop);
                } else {
                    int a = exist.getQuantity() == null ? 0 : exist.getQuantity();
                    int b = drop.getQuantity() == null ? 0 : drop.getQuantity();
                    exist.setQuantity(a + b);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * 结算掉落并发放到仓库
     */
    @Transactional(rollbackFor = Exception.class)
    public List<MonsterDropResultVo> rollAndGrantToWarehouse(String uid, Collection<String> monsterIds) {
        List<MonsterDropResultVo> drops = rollDrops(monsterIds);
        for (MonsterDropResultVo drop : drops) {
            warehouseService.addItem(uid, drop.getItemId(), drop.getQuantity());
        }
        return drops;
    }

    /**
     * 掉落率独立判定：100 = 100%
     */
    public static boolean rollRate(BigDecimal dropRate) {
        BigDecimal rate = dropRate == null ? BigDecimal.ZERO : dropRate;
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (rate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return true;
        }
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(100D)).compareTo(rate) < 0;
    }

    /**
     * 数量：min=max 固定；否则非线性偏小值
     */
    public static int rollQuantity(Integer minQty, Integer maxQty) {
        int min = minQty == null ? 1 : minQty;
        int max = maxQty == null ? min : maxQty;
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        return DropRandomUnit.randomDrop(min, max);
    }

    public void fillItemName(List<MonsterDrop> list) {
        if (list == null) {
            return;
        }
        for (MonsterDrop row : list) {
            Item item = itemService.getById(row.getItemId());
            if (item != null) {
                row.setItemName(item.getName());
            }
        }
    }

    /**
     * 关卡预览用：多怪物掉落配置去重合并（同物品取更高概率、更宽数量区间）
     */
    public List<StageDropPreviewVo> catalogPreviewByMonsterIds(Collection<String> monsterIds) {
        if (monsterIds == null || monsterIds.isEmpty()) {
            return List.of();
        }
        Map<String, StageDropPreviewVo> merged = new LinkedHashMap<>();
        for (String monsterId : monsterIds) {
            if (Wx.isEmpty(monsterId)) {
                continue;
            }
            for (MonsterDrop row : listEnabledByMonsterId(monsterId)) {
                if (row == null || Wx.isEmpty(row.getItemId())) {
                    continue;
                }
                StageDropPreviewVo exist = merged.get(row.getItemId());
                if (exist == null) {
                    StageDropPreviewVo vo = new StageDropPreviewVo();
                    vo.setItemId(row.getItemId());
                    vo.setItemName(row.getItemName() != null ? row.getItemName() : row.getItemId());
                    vo.setDropRate(row.getDropRate());
                    vo.setMinQty(row.getMinQty() == null ? 1 : row.getMinQty());
                    vo.setMaxQty(row.getMaxQty() == null ? vo.getMinQty() : row.getMaxQty());
                    merged.put(row.getItemId(), vo);
                    continue;
                }
                BigDecimal rate = row.getDropRate() == null ? BigDecimal.ZERO : row.getDropRate();
                BigDecimal old = exist.getDropRate() == null ? BigDecimal.ZERO : exist.getDropRate();
                if (rate.compareTo(old) > 0) {
                    exist.setDropRate(rate);
                }
                int min = row.getMinQty() == null ? 1 : row.getMinQty();
                int max = row.getMaxQty() == null ? min : row.getMaxQty();
                if (exist.getMinQty() == null || min < exist.getMinQty()) {
                    exist.setMinQty(min);
                }
                if (exist.getMaxQty() == null || max > exist.getMaxQty()) {
                    exist.setMaxQty(max);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }
}
