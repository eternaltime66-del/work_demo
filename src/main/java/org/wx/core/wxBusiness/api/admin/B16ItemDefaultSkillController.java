package org.wx.core.wxBusiness.api.admin;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;
import org.wx.core.wxBusiness.game.entity.vo.ItemDefaultSkillSaveReq;
import org.wx.core.wxBusiness.game.service.ItemDefaultSkillService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-装备默认充能技能
 */
@RestController
@RequestMapping("/back/item/default/skill")
public class B16ItemDefaultSkillController {

    @Resource
    public ItemDefaultSkillService itemDefaultSkillService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemDefaultSkill>> list(@ParamCheck String itemId) {
        return WxResult.success(itemDefaultSkillService.listByItemId(itemId));
    }

    @PostMapping("/save")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> save(@RequestBody ItemDefaultSkillSaveReq req) {
        itemDefaultSkillService.saveForItem(
                req != null ? req.getItemId() : null,
                req != null ? req.getSkills() : null
        );
        return WxResult.success();
    }
}
