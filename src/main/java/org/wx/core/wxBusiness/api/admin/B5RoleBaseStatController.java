package org.wx.core.wxBusiness.api.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.RoleBaseStat;
import org.wx.core.wxBusiness.game.service.RoleBaseStatService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-角色基础数值配置
 */
@RestController
@RequestMapping("/back/role/base/stat")
public class B5RoleBaseStatController {

    @Resource
    public RoleBaseStatService roleBaseStatService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<RoleBaseStat>> list(@RequestBody RoleBaseStat entity) {
        entity.clearEmptyString();
        IPage<RoleBaseStat> page = roleBaseStatService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody RoleBaseStat entity) {
        entity.clearEmptyString();
        roleBaseStatService.savePrepared(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody RoleBaseStat entity) {
        roleBaseStatService.removeById(entity.getId());
        return WxResult.success();
    }
}
