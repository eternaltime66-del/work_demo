package org.wx.core.wxBusiness.api.user;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Recipe;
import org.wx.core.wxBusiness.game.service.RecipeService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-合成配方
 */
@RestController
@RequestMapping("/api/recipe")
public class A9RecipeController {

    @Resource
    public RecipeService recipeService;

    /**
     * 启用中的配方列表（含材料）
     */
    @PostMapping("/list")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Recipe>> list() {
        return WxResult.success(recipeService.listEnabledWithMaterials());
    }
}
