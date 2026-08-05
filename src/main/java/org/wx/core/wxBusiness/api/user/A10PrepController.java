package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.vo.BattleBagVo;
import org.wx.core.wxBusiness.game.entity.vo.PrepSummaryVo;
import org.wx.core.wxBusiness.game.service.PrepService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前端-备战（仓库 / 战斗背包 / 装备）
 */
@RestController
@RequestMapping("/api/prep")
public class A10PrepController {

    @Resource
    private PrepService prepService;

    @PostMapping("/summary")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<PrepSummaryVo> summary() {
        return WxResult.success(prepService.getSummary(Wx.memberId()));
    }

    @PostMapping("/transfer")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<?> transfer(
            @ParamCheck(msg = "来源类型") String fromType,
            @ParamCheck(msg = "来源标识") String fromKey,
            @ParamCheck(msg = "目标类型") String toType,
            @ParamCheck(msg = "目标标识", notNull = false) String toKey,
            @ParamCheck(msg = "数量", notNull = false) Integer quantity
    ) {
        prepService.dragTransfer(Wx.memberId(), fromType, fromKey, toType, toKey, quantity);
        return WxResult.success();
    }

    @PostMapping("/warehouse/to-bag")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<?> warehouseToBag(@ParamCheck(msg = "仓库格子") String slotNos) {
        prepService.batchWarehouseToBag(Wx.memberId(), parseSlotNos(slotNos));
        return WxResult.success();
    }

    @PostMapping("/bag/to-warehouse")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<?> bagToWarehouse(@ParamCheck(msg = "背包条目ID") String bagIds) {
        prepService.batchBagToWarehouse(Wx.memberId(), parseIds(bagIds));
        return WxResult.success();
    }

    @PostMapping("/equip")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<BattleBagVo> equip(
            @ParamCheck(msg = "槽位") String slot,
            @ParamCheck(msg = "物品ID") String itemId
    ) {
        return WxResult.success(prepService.equipSlot(Wx.memberId(), slot, itemId));
    }

    @PostMapping("/unequip")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<BattleBagVo> unequip(@ParamCheck(msg = "槽位") String slot) {
        return WxResult.success(prepService.unequipSlot(Wx.memberId(), slot));
    }

    private List<String> parseIds(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Integer> parseSlotNos(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}
