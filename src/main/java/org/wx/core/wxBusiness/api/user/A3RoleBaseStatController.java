package org.wx.core.wxBusiness.api.user;

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
 * 前端-角色基础数值配置列表
 */
@RestController
@RequestMapping("/api/role/base/stat")
public class A3RoleBaseStatController {

    @Resource
    public RoleBaseStatService roleBaseStatService;

    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<RoleBaseStat>> list(@RequestBody(required = false) RoleBaseStat entity) {
        if (entity == null) {
            entity = new RoleBaseStat();
        }
        entity.clearEmptyString();
        IPage<RoleBaseStat> page = roleBaseStatService.pageQuery(entity);
        return WxResult.page(page);
    }
}
