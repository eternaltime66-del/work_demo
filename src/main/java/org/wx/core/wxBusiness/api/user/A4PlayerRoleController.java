package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.service.PlayerRoleService;
import org.wx.core.wxBusiness.game.service.PrepService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-我的角色
 */
@RestController
@RequestMapping("/api/player/role")
public class A4PlayerRoleController {

    @Resource
    public PlayerRoleService playerRoleService;
    @Resource
    private PrepService prepService;

    /**
     * 我的角色列表
     */
    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<PlayerRole>> list() {
        String uid = Wx.memberId();
        List<PlayerRole> list = playerRoleService.listByUid(uid);
        for (PlayerRole role : list) {
            prepService.fillDisplayStats(uid, role);
        }
        return WxResult.success(list);
    }

    /**
     * 我的角色详情
     */
    @PostMapping("/info")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<PlayerRole> info(@ParamCheck String id) {
        PlayerRole role = playerRoleService.getById(id);
        ErrorFactory.throwError(role == null, "角色不存在");
        ErrorFactory.throwError(!Wx.memberId().equals(role.getUid()), "无权查看");
        prepService.fillDisplayStats(Wx.memberId(), role);
        return WxResult.success(role);
    }

    /**
     * 按基础配置获得角色
     */
    @PostMapping("/create")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<PlayerRole> create(@ParamCheck String baseStatId) {
        return WxResult.success(playerRoleService.createFromBaseStat(Wx.memberId(), baseStatId));
    }
}
