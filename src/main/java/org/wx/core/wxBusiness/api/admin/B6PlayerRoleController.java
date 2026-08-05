package org.wx.core.wxBusiness.api.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.service.PlayerLayoutService;
import org.wx.core.wxBusiness.game.service.PlayerRoleService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-玩家持有角色
 */
@RestController
@RequestMapping("/back/player/role")
public class B6PlayerRoleController {

    @Resource
    public PlayerRoleService playerRoleService;
    @Resource
    public PlayerLayoutService playerLayoutService;

    /**
     * 玩家角色列表（可按 uid 筛选）
     */
    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<PlayerRole>> list(@RequestBody PlayerRole entity) {
        entity.clearEmptyString();
        IPage<PlayerRole> page = playerRoleService.pageQuery(entity);
        return WxResult.page(page);
    }

    /**
     * 按基础配置给玩家发放角色
     */
    @PostMapping("/grant")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<PlayerRole> grant(
            @ParamCheck String uid,
            @ParamCheck String baseStatId
    ) {
        return WxResult.success(playerRoleService.createFromBaseStat(uid, baseStatId));
    }

    /**
     * 修改玩家角色数值
     */
    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody PlayerRole entity) {
        entity.clearEmptyString();
        playerRoleService.prepareRatios(entity);
        playerRoleService.saveOrUpdate(entity);
        return WxResult.success();
    }

    /**
     * 删除玩家角色
     */
    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody PlayerRole entity) {
        playerLayoutService.removeByRoleId(entity.getId());
        playerRoleService.removeById(entity.getId());
        return WxResult.success();
    }
}
