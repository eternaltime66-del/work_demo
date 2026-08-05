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
import org.wx.core.wxBusiness.game.entity.SkillEffectGroup;
import org.wx.core.wxBusiness.game.service.SkillEffectGroupService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-快捷效果组
 */
@RestController
@RequestMapping("/back/skill/effect/group")
public class B15SkillEffectGroupController {

    @Resource
    public SkillEffectGroupService skillEffectGroupService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<SkillEffectGroup>> list(@RequestBody SkillEffectGroup entity) {
        entity.clearEmptyString();
        IPage<SkillEffectGroup> page = skillEffectGroupService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody SkillEffectGroup entity) {
        entity.clearEmptyString();
        skillEffectGroupService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody SkillEffectGroup entity) {
        skillEffectGroupService.removeById(entity.getId());
        return WxResult.success();
    }
}
