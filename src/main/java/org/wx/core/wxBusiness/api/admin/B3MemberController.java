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
import org.wx.core.wxBusiness.game.service.WarehouseService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

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

}
