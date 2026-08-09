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
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.service.PassiveSkillService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-被动技能（基础型 / 战斗型，技能 V2）
 */
@RestController
@RequestMapping("/back/passive/skill")
public class B14PassiveSkillController {

    @Resource
    public PassiveSkillService passiveSkillService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<PassiveSkill>> list(@RequestBody PassiveSkill entity) {
        entity.clearEmptyString();
        IPage<PassiveSkill> page = passiveSkillService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/detail")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<PassiveSkill> detail(@ParamCheck String id) {
        return WxResult.success(passiveSkillService.getDetail(id));
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody PassiveSkill entity) {
        entity.clearEmptyString();
        passiveSkillService.saveWithConditions(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody PassiveSkill entity) {
        passiveSkillService.removeWithConditions(entity.getId());
        return WxResult.success();
    }
}
