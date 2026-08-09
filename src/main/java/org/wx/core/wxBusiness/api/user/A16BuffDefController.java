package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.service.BuffDefService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

/**
 * 前端-BUFF 定义详情（只读）
 */
@RestController
@RequestMapping("/api/buff/def")
public class A16BuffDefController {

    @Resource
    private BuffDefService buffDefService;

    @PostMapping("/detail")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<BuffDef> detail(@ParamCheck String id) {
        BuffDef def = buffDefService.getById(id);
        ErrorFactory.throwError(def == null, "BUFF 不存在");
        ErrorFactory.throwError(Boolean.FALSE.equals(def.getEnable()), "BUFF 已停用");
        return WxResult.success(def);
    }
}
