package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.PlayerLayout;
import org.wx.core.wxBusiness.game.service.PlayerLayoutService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-玩家布阵（横6竖5）
 */
@RestController
@RequestMapping("/api/player/layout")
public class A6PlayerLayoutController {

    @Resource
    public PlayerLayoutService playerLayoutService;

    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<PlayerLayout>> list() {
        return WxResult.success(playerLayoutService.listOrDefault(Wx.memberId()));
    }

    @PostMapping("/save")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<PlayerLayout>> save(@RequestBody List<PlayerLayout> items) {
        return WxResult.success(playerLayoutService.replaceAll(Wx.memberId(), items));
    }
}
