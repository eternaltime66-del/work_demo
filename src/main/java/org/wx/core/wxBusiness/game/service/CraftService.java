package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.annotation.RedisLock;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.Recipe;
import org.wx.core.wxBusiness.game.entity.RecipeMaterial;
import org.wx.core.wxBusiness.game.entity.vo.CraftMaterialVo;
import org.wx.core.wxBusiness.game.entity.vo.CraftRecipeVo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CraftService {

    @Resource
    private RecipeService recipeService;
    @Resource
    private RecipeMaterialService recipeMaterialService;
    @Resource
    private ItemService itemService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private BattleBagService battleBagService;
    @Resource
    private ItemDropSourceService itemDropSourceService;
    @Resource
    private ItemDetailService itemDetailService;
    @Resource
    private StageProgressService stageProgressService;

    public List<CraftRecipeVo> listRecipes(String uid) {
        List<Recipe> recipes = recipeService.listEnabledWithMaterials();
        Map<String, Integer> owned = warehouseService.countItems(uid);
        Map<String, String> outputToRecipe = buildOutputRecipeIndex(recipes);
        Map<String, Item> itemIndex = buildItemIndex(recipes);
        Set<String> materialItemIds = new HashSet<>();
        for (Recipe recipe : recipes) {
            if (recipe.getMaterials() == null) {
                continue;
            }
            for (RecipeMaterial material : recipe.getMaterials()) {
                if (material != null && !Wx.isEmpty(material.getItemId())) {
                    materialItemIds.add(material.getItemId());
                }
            }
        }
        Set<String> dropSourceItemIds = itemDropSourceService.listItemIdsWithDropSource(materialItemIds);
        Set<String> lockedChapterIds = new HashSet<>();
        for (Recipe recipe : recipes) {
            if (recipe != null && !Wx.isEmpty(recipe.getUnlockChapterId())) {
                lockedChapterIds.add(recipe.getUnlockChapterId());
            }
        }
        Set<String> unlockedChapterIds = stageProgressService.listUnlockedChapterIds(uid, lockedChapterIds);
        List<CraftRecipeVo> list = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!Wx.isEmpty(recipe.getUnlockChapterId())
                    && !unlockedChapterIds.contains(recipe.getUnlockChapterId())) {
                continue;
            }
            list.add(buildVo(recipe, owned, outputToRecipe, false, itemIndex, dropSourceItemIds));
        }
        return list;
    }

    public CraftRecipeVo getRecipe(String uid, String recipeId) {
        Recipe recipe = getEnabled(recipeId);
        ErrorFactory.throwError(!isRecipeUnlocked(uid, recipe), "配方未解锁，需进入对应章节");
        recipeService.fillOutputName(recipe);
        recipe.setMaterials(recipeMaterialService.listByRecipeId(recipe.getId()));
        Map<String, String> outputToRecipe = buildOutputRecipeIndex(recipeService.listEnabledWithMaterials());
        return buildVo(recipe, warehouseService.countItems(uid), outputToRecipe, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid", bindMethod = false, loading = true)
    public CraftRecipeVo craft(String uid, String recipeId) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "未登录");
        Recipe recipe = getEnabled(recipeId);
        ErrorFactory.throwError(!isRecipeUnlocked(uid, recipe), "配方未解锁，需进入对应章节");
        List<RecipeMaterial> materials = recipeMaterialService.listByRecipeId(recipeId);
        ErrorFactory.throwError(materials == null || materials.isEmpty(), "配方材料未配置");

        Map<String, Integer> owned = warehouseService.countItems(uid);
        for (RecipeMaterial material : materials) {
            int need = material.getQuantity() == null ? 0 : material.getQuantity();
            int have = owned.getOrDefault(material.getItemId(), 0);
            Item item = itemService.getById(material.getItemId());
            String name = item != null ? item.getName() : material.getItemId();
            ErrorFactory.throwError(have < need, name + " 不足，需要 " + need + "，当前 " + have);
        }
        for (RecipeMaterial material : materials) {
            int need = material.getQuantity() == null ? 0 : material.getQuantity();
            if (need > 0) {
                warehouseService.consumeItem(uid, material.getItemId(), need);
            }
        }
        int outQty = recipe.getOutputQty() == null || recipe.getOutputQty() < 1 ? 1 : recipe.getOutputQty();
        // 合成产物进战斗背包（材料仍从仓库扣除）
        battleBagService.addItem(uid, recipe.getOutputItemId(), outQty);
        return getRecipe(uid, recipeId);
    }

    /** 空章节锁=不限制；否则需对应主线章节已对玩家解锁 */
    private boolean isRecipeUnlocked(String uid, Recipe recipe) {
        if (recipe == null) {
            return false;
        }
        if (Wx.isEmpty(recipe.getUnlockChapterId())) {
            return true;
        }
        return stageProgressService.isChapterUnlocked(uid, recipe.getUnlockChapterId());
    }

    private Recipe getEnabled(String recipeId) {
        Recipe recipe = recipeService.getById(recipeId);
        ErrorFactory.notNull(recipe, "配方不存在");
        ErrorFactory.throwError(!Boolean.TRUE.equals(recipe.getEnable()), "配方未启用");
        return recipe;
    }

    private Map<String, String> buildOutputRecipeIndex(List<Recipe> recipes) {
        Map<String, String> map = new HashMap<>();
        for (Recipe r : recipes) {
            if (r != null && !Wx.isEmpty(r.getOutputItemId())) {
                map.putIfAbsent(r.getOutputItemId(), r.getId());
            }
        }
        return map;
    }

    private CraftRecipeVo buildVo(Recipe recipe, Map<String, Integer> owned, Map<String, String> outputToRecipe, boolean withItemDetail) {
        return buildVo(recipe, owned, outputToRecipe, withItemDetail, null, null);
    }

    private CraftRecipeVo buildVo(
            Recipe recipe,
            Map<String, Integer> owned,
            Map<String, String> outputToRecipe,
            boolean withItemDetail,
            Map<String, Item> itemIndex,
            Set<String> dropSourceItemIds
    ) {
        CraftRecipeVo vo = new CraftRecipeVo();
        vo.setId(recipe.getId());
        vo.setRemark(recipe.getRemark());
        vo.setResultItemId(recipe.getOutputItemId());
        vo.setResultQty(recipe.getOutputQty() == null || recipe.getOutputQty() < 1 ? 1 : recipe.getOutputQty());

        Item result = itemIndex != null
                ? itemIndex.get(recipe.getOutputItemId())
                : itemService.getById(recipe.getOutputItemId());
        if (result != null) {
            vo.setName(result.getName());
            vo.setResultItemName(result.getName());
            vo.setResultItemIcon(result.getIcon());
            if (result.getItemType() != null) {
                vo.setResultItemType(result.getItemType().name());
            }
        } else {
            vo.setName(!Wx.isEmpty(recipe.getName()) ? recipe.getName() : recipe.getId());
            vo.setResultItemName(vo.getName());
        }
        if (!Wx.isEmpty(recipe.getName())) {
            vo.setName(recipe.getName());
        }
        if (withItemDetail) {
            vo.setResultItem(itemDetailService.buildRich(result));
        }

        List<CraftMaterialVo> materials = new ArrayList<>();
        List<CraftMaterialVo> missing = new ArrayList<>();
        boolean canCraft = true;
        List<RecipeMaterial> mats = recipe.getMaterials();
        if (mats == null) {
            mats = recipeMaterialService.listByRecipeId(recipe.getId());
        }
        for (RecipeMaterial material : mats) {
            CraftMaterialVo mv = new CraftMaterialVo();
            mv.setItemId(material.getItemId());
            int need = material.getQuantity() == null ? 0 : material.getQuantity();
            int have = owned.getOrDefault(material.getItemId(), 0);
            mv.setRequiredQty(need);
            mv.setOwnedQty(have);
            int miss = Math.max(0, need - have);
            mv.setMissingQty(miss);
            mv.setEnough(miss <= 0);
            Item item = itemIndex != null
                    ? itemIndex.get(material.getItemId())
                    : itemService.getById(material.getItemId());
            if (item != null) {
                mv.setItemName(item.getName());
                mv.setIcon(item.getIcon());
            } else {
                mv.setItemName(material.getItemName() != null ? material.getItemName() : material.getItemId());
            }
            fillSource(mv, outputToRecipe, dropSourceItemIds);
            materials.add(mv);
            if (miss > 0) {
                missing.add(mv);
                canCraft = false;
            }
        }
        vo.setMaterials(materials);
        vo.setMissingMaterials(missing);
        vo.setCanCraft(canCraft);
        return vo;
    }

    private void fillSource(
            CraftMaterialVo mv,
            Map<String, String> outputToRecipe,
            Set<String> dropSourceItemIds
    ) {
        boolean hasDropSource = dropSourceItemIds != null
                ? dropSourceItemIds.contains(mv.getItemId())
                : itemDropSourceService.hasDropSource(mv.getItemId());
        if (hasDropSource) {
            mv.setSourceType("BATTLE");
            mv.setSourceLabel("去获取");
            return;
        }
        String recipeId = outputToRecipe.get(mv.getItemId());
        if (!Wx.isEmpty(recipeId)) {
            mv.setSourceType("CRAFT");
            mv.setSourceLabel("去合成");
            mv.setSourceRecipeId(recipeId);
            return;
        }
        mv.setSourceType("NONE");
        mv.setSourceLabel("敬请期待");
    }

    private Map<String, Item> buildItemIndex(List<Recipe> recipes) {
        Set<String> itemIds = new HashSet<>();
        for (Recipe recipe : recipes) {
            if (recipe == null) {
                continue;
            }
            if (!Wx.isEmpty(recipe.getOutputItemId())) {
                itemIds.add(recipe.getOutputItemId());
            }
            if (recipe.getMaterials() != null) {
                for (RecipeMaterial material : recipe.getMaterials()) {
                    if (material != null && !Wx.isEmpty(material.getItemId())) {
                        itemIds.add(material.getItemId());
                    }
                }
            }
        }
        Map<String, Item> itemIndex = new LinkedHashMap<>();
        if (!itemIds.isEmpty()) {
            for (Item item : itemService.listByIds(itemIds)) {
                itemIndex.put(item.getId(), item);
            }
        }
        return itemIndex;
    }
}
