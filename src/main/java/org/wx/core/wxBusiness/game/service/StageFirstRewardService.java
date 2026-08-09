package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.StageFirstReward;
import org.wx.core.wxBusiness.game.entity.vo.StageFirstRewardVo;
import org.wx.core.wxBusiness.game.mapper.StageFirstRewardMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StageFirstRewardService extends WxServiceImpl<StageFirstRewardMapper, StageFirstReward> {

    @Resource
    private ItemService itemService;
    @Resource
    private WarehouseService warehouseService;

    public List<StageFirstReward> listByStageId(String stageId) {
        if (Wx.isEmpty(stageId)) {
            return List.of();
        }
        return this.find()
                .eq(StageFirstReward::getStageId, stageId)
                .orderByAsc(StageFirstReward::getSort)
                .list();
    }

    public List<StageFirstRewardVo> listVoByStageId(String stageId) {
        List<StageFirstReward> rows = listByStageId(stageId);
        List<StageFirstRewardVo> out = new ArrayList<>();
        for (StageFirstReward row : rows) {
            StageFirstRewardVo vo = new StageFirstRewardVo();
            vo.setId(row.getId());
            vo.setStageId(row.getStageId());
            vo.setItemId(row.getItemId());
            vo.setQty(row.getQty() == null ? 1 : row.getQty());
            vo.setSort(row.getSort() == null ? 0 : row.getSort());
            Item item = itemService.getById(row.getItemId());
            vo.setItemName(item != null ? item.getName() : row.getItemId());
            out.add(vo);
        }
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceRewards(String stageId, List<Map<String, Object>> rewards) {
        ErrorFactory.throwError(Wx.isEmpty(stageId), "关卡不能为空");
        this.remove(this.find().eq(StageFirstReward::getStageId, stageId).wrapper());
        if (rewards == null || rewards.isEmpty()) {
            return;
        }
        int i = 0;
        for (Map<String, Object> r : rewards) {
            if (r == null) {
                continue;
            }
            Object itemIdObj = r.get("itemId");
            if (itemIdObj == null || Wx.isEmpty(String.valueOf(itemIdObj))) {
                continue;
            }
            String itemId = String.valueOf(itemIdObj);
            Item item = itemService.getById(itemId);
            ErrorFactory.throwError(item == null, "物品不存在: " + itemId);
            int qty = 1;
            Object qtyObj = r.get("qty");
            if (qtyObj != null) {
                qty = Math.max(1, Integer.parseInt(String.valueOf(qtyObj)));
            }
            StageFirstReward row = new StageFirstReward();
            row.setStageId(stageId);
            row.setItemId(itemId);
            row.setQty(qty);
            row.setSort(i++);
            this.save(row);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StageFirstRewardVo> grantToWarehouse(String uid, String stageId) {
        List<StageFirstRewardVo> vos = listVoByStageId(stageId);
        for (StageFirstRewardVo vo : vos) {
            warehouseService.addItem(uid, vo.getItemId(), vo.getQty() == null ? 1 : vo.getQty());
        }
        return vos;
    }
}
