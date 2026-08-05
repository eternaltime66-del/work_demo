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
import org.wx.core.wxBusiness.game.entity.Recipe;
import org.wx.core.wxBusiness.game.entity.RecipeMaterial;
import org.wx.core.wxBusiness.game.service.RecipeMaterialService;
import org.wx.core.wxBusiness.game.service.RecipeService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-合成配方
 */
@RestController
@RequestMapping("/back/recipe")
public class B12RecipeController {

    @Resource
    public RecipeService recipeService;
    @Resource
    public RecipeMaterialService recipeMaterialService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Recipe>> list(@RequestBody Recipe entity) {
        entity.clearEmptyString();
        IPage<Recipe> page = recipeService.pageQuery(entity);
        for (Recipe recipe : page.getRecords()) {
            recipeService.fillOutputName(recipe);
        }
        return WxResult.page(page);
    }

    @PostMapping("/detail")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<Recipe> detail(@ParamCheck String id) {
        return WxResult.success(recipeService.getDetail(id));
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody Recipe entity) {
        entity.clearEmptyString();
        recipeService.saveWithMaterials(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody Recipe entity) {
        recipeService.removeWithMaterials(entity.getId());
        return WxResult.success();
    }

    @PostMapping("/material/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<RecipeMaterial>> materialList(@ParamCheck String recipeId) {
        return WxResult.success(recipeMaterialService.listByRecipeId(recipeId));
    }

    @PostMapping("/material/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> materialUpdate(@RequestBody RecipeMaterial entity) {
        entity.clearEmptyString();
        recipeMaterialService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/material/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> materialRemove(@RequestBody RecipeMaterial entity) {
        recipeMaterialService.removeById(entity.getId());
        return WxResult.success();
    }
}
