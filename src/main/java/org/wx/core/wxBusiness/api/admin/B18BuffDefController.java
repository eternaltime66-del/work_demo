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
import org.wx.core.wxBusiness.game.entity.BuffDef;
import org.wx.core.wxBusiness.game.service.BuffDefService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-标准化 BUFF 定义（技能 V2）
 */
@RestController
@RequestMapping("/back/buff/def")
public class B18BuffDefController {

    @Resource
    private BuffDefService buffDefService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<BuffDef>> list(@RequestBody BuffDef entity) {
        entity.clearEmptyString();
        IPage<BuffDef> page = buffDefService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/detail")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<BuffDef> detail(@ParamCheck String id) {
        return WxResult.success(buffDefService.getById(id));
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody BuffDef entity) {
        entity.clearEmptyString();
        buffDefService.saveValidated(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody BuffDef entity) {
        buffDefService.removeById(entity.getId());
        return WxResult.success();
    }

    /** 下拉选项（启用中） */
    @PostMapping("/options")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<BuffDef>> options() {
        return WxResult.success(buffDefService.listEnabledOptions());
    }
}
