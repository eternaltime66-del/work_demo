package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.BattleBag;
import org.wx.core.wxBusiness.game.service.BattleBagService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-战斗背包
 */
@RestController
@RequestMapping("/api/battle/bag")
public class A8BattleBagController {

    @Resource
    public BattleBagService battleBagService;

    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<BattleBag>> list() {
        return WxResult.success(battleBagService.listByUid(Wx.memberId()));
    }
}
