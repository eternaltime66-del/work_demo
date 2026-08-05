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
import org.wx.core.wxBusiness.game.entity.SkillChargeGroup;
import org.wx.core.wxBusiness.game.service.SkillChargeGroupService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-快捷充能组
 */
@RestController
@RequestMapping("/back/skill/charge/group")
public class B14SkillChargeGroupController {

    @Resource
    public SkillChargeGroupService skillChargeGroupService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<SkillChargeGroup>> list(@RequestBody SkillChargeGroup entity) {
        entity.clearEmptyString();
        IPage<SkillChargeGroup> page = skillChargeGroupService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody SkillChargeGroup entity) {
        entity.clearEmptyString();
        skillChargeGroupService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody SkillChargeGroup entity) {
        skillChargeGroupService.removeById(entity.getId());
        return WxResult.success();
    }
}
