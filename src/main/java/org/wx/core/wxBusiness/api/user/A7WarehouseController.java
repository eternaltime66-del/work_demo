package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Warehouse;
import org.wx.core.wxBusiness.game.entity.WarehouseItem;
import org.wx.core.wxBusiness.game.service.WarehouseService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端-仓库
 */
@RestController
@RequestMapping("/api/warehouse")
public class A7WarehouseController {

    @Resource
    public WarehouseService warehouseService;

    /**
     * 我的仓库：确保仓库存在并返回物品列表
     */
    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<Map<String, Object>> list() {
        String uid = Wx.memberId();
        Warehouse warehouse = warehouseService.ensureWarehouse(uid);
        List<WarehouseItem> items = warehouseService.listItems(uid);
        Map<String, Object> data = new HashMap<>();
        data.put("warehouse", warehouse);
        data.put("items", items);
        return WxResult.success(data);
    }
}
