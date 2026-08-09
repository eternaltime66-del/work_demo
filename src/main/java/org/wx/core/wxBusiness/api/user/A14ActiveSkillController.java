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
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.service.ActiveSkillService;
import org.wx.core.wxBusiness.game.service.SkillChargeService;
import org.wx.core.wxBusiness.game.service.SkillEffectService;
import org.wx.core.wxBusiness.game.service.SkillOutputService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端-主动技能详情（只读）
 */
@RestController
@RequestMapping("/api/active/skill")
public class A14ActiveSkillController {

    @Resource
    private ActiveSkillService activeSkillService;
    @Resource
    private SkillChargeService skillChargeService;
    @Resource
    private SkillEffectService skillEffectService;
    @Resource
    private SkillOutputService skillOutputService;

    @PostMapping("/detail")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<Map<String, Object>> detail(@ParamCheck String id) {
        ActiveSkill skill = activeSkillService.getById(id);
        ErrorFactory.throwError(skill == null, "技能不存在");
        ErrorFactory.throwError(Boolean.FALSE.equals(skill.getEnable()), "技能已停用");
        List<SkillCharge> charges = skillChargeService.listBySkillId(id);
        skillChargeService.fillMatchSkillName(charges, activeSkillService::getById);
        List<SkillOutput> outputs = skillOutputService.listBySkillId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("skill", skill);
        data.put("charges", charges);
        data.put("effects", skillEffectService.listBySkillId(id));
        data.put("outputs", outputs);
        return WxResult.success(data);
    }
}
