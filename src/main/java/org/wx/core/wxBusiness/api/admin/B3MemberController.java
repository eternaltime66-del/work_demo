package org.wx.core.wxBusiness.api.admin;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.Member;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.account.service.MemberService;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.service.PlayerDataResetService;
import org.wx.core.wxBusiness.game.service.PlayerStaminaService;
import org.wx.core.wxBusiness.game.service.WarehouseService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-普通用户
 */
@RestController
@RequestMapping("/back/member")
public class B3MemberController {

    @Resource
    public MemberService memberService;
    @Resource
    public WarehouseService warehouseService;
    @Resource
    public PlayerDataResetService playerDataResetService;
    @Resource
    public PlayerStaminaService playerStaminaService;

    /**
     * 用户列表
     */
    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Member>> defList(
            @RequestBody Member entity
    ) {
        return WxResult.page(memberService.pageQuery(entity));
    }

    /**
     * 赠送物品到玩家仓库
     */
    @PostMapping("/gift")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> gift(
            @ParamCheck(msg = "玩家") String uid,
            @ParamCheck(msg = "物品") String itemId,
            @ParamCheck(msg = "数量") Integer quantity
    ) {
        warehouseService.addItem(uid, itemId, quantity == null ? 0 : quantity);
        return WxResult.success();
    }

    /**
     * 赠送体力（可超过上限）
     */
    @PostMapping("/giftStamina")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<Map<String, Object>> giftStamina(
            @ParamCheck(msg = "玩家") String uid,
            @ParamCheck(msg = "体力") Integer amount
    ) {
        PlayerStamina row = playerStaminaService.add(uid, amount == null ? 0 : amount);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stamina", row.getStamina());
        m.put("maxStamina", row.getMaxStamina());
        return WxResult.success(m);
    }

    /**
     * 重置玩家游戏数据：角色/装备/背包/仓库物品/关卡进度/体力/爬塔；
     * 保留账号与钱包，并重新发放默认角色与空仓库。
     */
    @PostMapping("/resetData")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> resetData(@ParamCheck(msg = "玩家") String uid) {
        playerDataResetService.resetPlayerData(uid);
        return WxResult.success();
    }

}
