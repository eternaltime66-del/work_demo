package org.wx.core.wxBusiness.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.Recipe;
import org.wx.core.wxBusiness.game.entity.RecipeMaterial;
import org.wx.core.wxBusiness.game.mapper.RecipeMapper;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService extends WxServiceImpl<RecipeMapper, Recipe> {

    @Resource
    private RecipeMaterialService recipeMaterialService;
    @Resource
    private ItemService itemService;

    /**
     * 启用中的配方列表，并填充产出名与材料
     */
    public List<Recipe> listEnabledWithMaterials() {
        List<Recipe> list = this.find()
                .eq(Recipe::getEnable, true)
                .orderByAsc(Recipe::getSort)
                .list();
        if (list.isEmpty()) {
            return list;
        }
        Map<String, List<RecipeMaterial>> materialsByRecipe = recipeMaterialService
                .listByRecipeIds(list.stream().map(Recipe::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(RecipeMaterial::getRecipeId));
        Map<String, Item> outputItems = new HashMap<>();
        List<String> outputIds = list.stream()
                .map(Recipe::getOutputItemId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (!outputIds.isEmpty()) {
            for (Item item : itemService.listByIds(outputIds)) {
                outputItems.put(item.getId(), item);
            }
        }
        for (Recipe recipe : list) {
            Item output = outputItems.get(recipe.getOutputItemId());
            if (output != null) {
                recipe.setOutputItemName(output.getName());
            }
            recipe.setMaterials(materialsByRecipe.getOrDefault(recipe.getId(), List.of()));
        }
        return list;
    }

    public Recipe getDetail(String id) {
        Recipe recipe = this.getById(id);
        ErrorFactory.notNull(recipe, "配方不存在");
        fillOutputName(recipe);
        recipe.setMaterials(recipeMaterialService.listByRecipeId(id));
        return recipe;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveWithMaterials(Recipe entity) {
        ErrorFactory.notEmpty(entity.getOutputItemId(), "请选择产出物品");
        Item output = itemService.getById(entity.getOutputItemId());
        ErrorFactory.notNull(output, "产出物品不存在");

        if (!StringUtils.hasText(entity.getName())) {
            entity.setName(output.getName() + "配方");
        }
        if (entity.getOutputQty() == null || entity.getOutputQty() < 1) {
            entity.setOutputQty(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnable() == null) {
            entity.setEnable(true);
        }

        List<RecipeMaterial> materials = entity.getMaterials();
        ErrorFactory.throwError(materials == null || materials.isEmpty(), "至少添加一种材料");

        Set<String> materialIds = new HashSet<>();
        for (RecipeMaterial mat : materials) {
            ErrorFactory.notEmpty(mat.getItemId(), "请选择材料物品");
            ErrorFactory.throwError(mat.getItemId().equals(entity.getOutputItemId()), "材料不能与产出物品相同");
            ErrorFactory.throwError(!materialIds.add(mat.getItemId()), "材料不能重复");
            Item materialItem = itemService.getById(mat.getItemId());
            ErrorFactory.notNull(materialItem, "材料物品不存在");
            int qty = mat.getQuantity() != null ? mat.getQuantity() : 1;
            ErrorFactory.throwError(qty <= 0, "材料数量必须大于 0");
            mat.setQuantity(qty);
        }

        this.saveOrUpdate(entity);
        ErrorFactory.notEmpty(entity.getId(), "配方保存失败");

        recipeMaterialService.remove(new LambdaQueryWrapper<RecipeMaterial>()
                .eq(RecipeMaterial::getRecipeId, entity.getId()));

        int sort = 1;
        for (RecipeMaterial mat : materials) {
            RecipeMaterial row = new RecipeMaterial();
            row.setRecipeId(entity.getId());
            row.setItemId(mat.getItemId());
            row.setQuantity(mat.getQuantity());
            row.setSort(mat.getSort() != null ? mat.getSort() : sort);
            recipeMaterialService.save(row);
            sort++;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeWithMaterials(String id) {
        ErrorFactory.notEmpty(id, "ID不能为空");
        recipeMaterialService.remove(new LambdaQueryWrapper<RecipeMaterial>()
                .eq(RecipeMaterial::getRecipeId, id));
        this.removeById(id);
    }

    public void fillOutputName(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        Item item = itemService.getById(recipe.getOutputItemId());
        if (item != null) {
            recipe.setOutputItemName(item.getName());
        }
    }
}
