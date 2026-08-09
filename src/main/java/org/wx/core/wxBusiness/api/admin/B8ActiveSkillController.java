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
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.SkillCharge;
import org.wx.core.wxBusiness.game.entity.SkillEffect;
import org.wx.core.wxBusiness.game.entity.SkillOutput;
import org.wx.core.wxBusiness.game.entity.vo.SkillOutputReplaceReq;
import org.wx.core.wxBusiness.game.service.ActiveSkillService;
import org.wx.core.wxBusiness.game.service.SkillChargeService;
import org.wx.core.wxBusiness.game.service.SkillEffectService;
import org.wx.core.wxBusiness.game.service.SkillOutputService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;
import org.wx.core.wxBase.factory.ErrorFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-主动技能（主表 + 充能配置 + V2 输出；旧效果接口保留兼容）
 */
@RestController
@RequestMapping("/back/active/skill")
public class B8ActiveSkillController {

    @Resource
    public ActiveSkillService activeSkillService;
    @Resource
    public SkillChargeService skillChargeService;
    @Resource
    public SkillEffectService skillEffectService;
    @Resource
    public SkillOutputService skillOutputService;

    // ---------- 技能主表 ----------

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ActiveSkill>> list(@RequestBody ActiveSkill entity) {
        entity.clearEmptyString();
        IPage<ActiveSkill> page = activeSkillService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody ActiveSkill entity) {
        entity.clearEmptyString();
        activeSkillService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody ActiveSkill entity) {
        activeSkillService.removeWithChildren(entity.getId());
        return WxResult.success();
    }

    /**
     * 技能详情：主表 + 充能 + V2 输出（outputs）+ 旧效果（effects，兼容回填）
     */
    @PostMapping("/detail")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<Map<String, Object>> detail(@ParamCheck String id) {
        List<SkillCharge> charges = skillChargeService.listBySkillId(id);
        skillChargeService.fillMatchSkillName(charges, activeSkillService::getById);
        Map<String, Object> data = new HashMap<>();
        data.put("skill", activeSkillService.getById(id));
        data.put("charges", charges);
        data.put("outputs", skillOutputService.listBySkillId(id));
        data.put("effects", skillEffectService.listBySkillId(id));
        return WxResult.success(data);
    }

    // ---------- 充能配置 ----------

    @PostMapping("/charge/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<SkillCharge>> chargeList(@ParamCheck String skillId) {
        return WxResult.success(skillChargeService.listBySkillId(skillId));
    }

    @PostMapping("/charge/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chargeUpdate(@RequestBody SkillCharge entity) {
        entity.clearEmptyString();
        skillChargeService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/charge/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> chargeRemove(@RequestBody SkillCharge entity) {
        skillChargeService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- V2 输出 ----------

    @PostMapping("/output/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<SkillOutput>> outputList(@ParamCheck String skillId) {
        return WxResult.success(skillOutputService.listBySkillId(skillId));
    }

    @PostMapping("/output/replace")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> outputReplace(@RequestBody SkillOutputReplaceReq req) {
        ErrorFactory.notNull(req, "参数不能为空");
        ErrorFactory.notEmpty(req.getSkillId(), "技能不能为空");
        skillOutputService.replaceForActiveSkill(req.getSkillId(), req.getOutputs());
        return WxResult.success();
    }

    @PostMapping("/output/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> outputUpdate(@RequestBody SkillOutput entity) {
        entity.clearEmptyString();
        skillOutputService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/output/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> outputRemove(@RequestBody SkillOutput entity) {
        skillOutputService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 旧效果配置（兼容） ----------

    @PostMapping("/effect/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<SkillEffect>> effectList(@ParamCheck String skillId) {
        return WxResult.success(skillEffectService.listBySkillId(skillId));
    }

    @PostMapping("/effect/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> effectUpdate(@RequestBody SkillEffect entity) {
        entity.clearEmptyString();
        skillEffectService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/effect/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> effectRemove(@RequestBody SkillEffect entity) {
        skillEffectService.removeById(entity.getId());
        return WxResult.success();
    }
}
