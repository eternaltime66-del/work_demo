package org.wx.core.wxBusiness.api.admin;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.ItemDefaultPassive;
import org.wx.core.wxBusiness.game.entity.vo.ItemDefaultPassiveSaveReq;
import org.wx.core.wxBusiness.game.service.ItemDefaultPassiveService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-装备默认被动
 */
@RestController
@RequestMapping("/back/item/default/passive")
public class B17ItemDefaultPassiveController {

    @Resource
    public ItemDefaultPassiveService itemDefaultPassiveService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemDefaultPassive>> list(@RequestBody ItemDefaultPassiveSaveReq req) {
        return WxResult.success(itemDefaultPassiveService.listByItemId(
                req != null ? req.getItemId() : null,
                req != null ? req.getPassiveType() : null
        ));
    }

    @PostMapping("/save")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> save(@RequestBody ItemDefaultPassiveSaveReq req) {
        // passiveType 为空：整表保存混合类型；否则按单类型替换
        if (req != null && req.getPassiveType() == null) {
            itemDefaultPassiveService.saveAllForItem(
                    req.getItemId(),
                    req.getPassives()
            );
        } else {
            itemDefaultPassiveService.saveForItem(
                    req != null ? req.getItemId() : null,
                    req != null ? req.getPassiveType() : null,
                    req != null ? req.getPassives() : null
            );
        }
        return WxResult.success();
    }
}
