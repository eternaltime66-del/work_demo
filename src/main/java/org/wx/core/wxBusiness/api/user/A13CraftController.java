package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.vo.CraftRecipeVo;
import org.wx.core.wxBusiness.game.entity.vo.ItemDropSourceVo;
import org.wx.core.wxBusiness.game.service.CraftService;
import org.wx.core.wxBusiness.game.service.ItemDropSourceService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-合成台
 */
@RestController
@RequestMapping("/api/craft")
public class A13CraftController {

    @Resource
    private CraftService craftService;
    @Resource
    private ItemDropSourceService itemDropSourceService;

    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<CraftRecipeVo>> list() {
        return WxResult.success(craftService.listRecipes(Wx.memberId()));
    }

    @PostMapping("/detail")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<CraftRecipeVo> detail(@ParamCheck(msg = "配方") String recipeId) {
        return WxResult.success(craftService.getRecipe(Wx.memberId(), recipeId));
    }

    @PostMapping("/execute")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<CraftRecipeVo> execute(@ParamCheck(msg = "配方") String recipeId) {
        return WxResult.success(craftService.craft(Wx.memberId(), recipeId));
    }

    @PostMapping("/drop-sources")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<ItemDropSourceVo>> dropSources(@ParamCheck(msg = "物品") String itemId) {
        return WxResult.success(itemDropSourceService.listByItemId(itemId));
    }
}
