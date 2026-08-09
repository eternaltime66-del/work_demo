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
import org.wx.core.wxBusiness.game.entity.PassiveSkill;
import org.wx.core.wxBusiness.game.service.PassiveSkillService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

/**
 * 前端-被动技能详情（只读）
 */
@RestController
@RequestMapping("/api/passive/skill")
public class A15PassiveSkillController {

    @Resource
    private PassiveSkillService passiveSkillService;

    @PostMapping("/detail")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<PassiveSkill> detail(@ParamCheck String id) {
        PassiveSkill skill = passiveSkillService.getDetail(id);
        ErrorFactory.throwError(skill == null, "被动技能不存在");
        ErrorFactory.throwError(Boolean.FALSE.equals(skill.getEnable()), "被动技能已停用");
        return WxResult.success(skill);
    }
}
