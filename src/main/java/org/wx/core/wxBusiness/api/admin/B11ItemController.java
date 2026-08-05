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
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;
import org.wx.core.wxBusiness.game.entity.ItemAccessory;
import org.wx.core.wxBusiness.game.entity.ItemArmor;
import org.wx.core.wxBusiness.game.entity.ItemGloves;
import org.wx.core.wxBusiness.game.entity.ItemHelmet;
import org.wx.core.wxBusiness.game.entity.ItemLegs;
import org.wx.core.wxBusiness.game.entity.ItemMaterial;
import org.wx.core.wxBusiness.game.entity.ItemWeapon;
import org.wx.core.wxBusiness.game.service.ItemAccessoryService;
import org.wx.core.wxBusiness.game.service.ItemArmorService;
import org.wx.core.wxBusiness.game.service.ItemDetailService;
import org.wx.core.wxBusiness.game.service.ItemGlovesService;
import org.wx.core.wxBusiness.game.service.ItemHelmetService;
import org.wx.core.wxBusiness.game.service.ItemLegsService;
import org.wx.core.wxBusiness.game.service.ItemMaterialService;
import org.wx.core.wxBusiness.game.service.ItemService;
import org.wx.core.wxBusiness.game.service.ItemWeaponService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-物品（主表 + 各类型扩展）
 */
@RestController
@RequestMapping("/back/item")
public class B11ItemController {

    @Resource
    public ItemService itemService;
    @Resource
    public ItemMaterialService itemMaterialService;
    @Resource
    public ItemWeaponService itemWeaponService;
    @Resource
    public ItemArmorService itemArmorService;
    @Resource
    public ItemGlovesService itemGlovesService;
    @Resource
    public ItemHelmetService itemHelmetService;
    @Resource
    public ItemAccessoryService itemAccessoryService;
    @Resource
    public ItemLegsService itemLegsService;
    @Resource
    public ItemDetailService itemDetailService;

    // ---------- 物品主表 ----------

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Item>> list(@RequestBody Item entity) {
        entity.clearEmptyString();
        IPage<Item> page = itemService.pageQuery(entity);
        return WxResult.page(page);
    }

    /** 装备/物品详情（含技能被动人性化文案） */
    @PostMapping("/detail")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<CraftItemDetailVo> detail(@ParamCheck String id) {
        CraftItemDetailVo vo = itemDetailService.buildRichByItemId(id);
        ErrorFactory.notNull(vo, "物品不存在");
        return WxResult.success(vo);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody Item entity) {
        entity.clearEmptyString();
        itemService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody Item entity) {
        itemService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 材料 ----------

    @PostMapping("/material/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemMaterial>> materialList(@RequestBody ItemMaterial entity) {
        entity.clearEmptyString();
        IPage<ItemMaterial> page = itemMaterialService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/material/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> materialUpdate(@RequestBody ItemMaterial entity) {
        entity.clearEmptyString();
        itemMaterialService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/material/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> materialRemove(@RequestBody ItemMaterial entity) {
        itemMaterialService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 武器 ----------

    @PostMapping("/weapon/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemWeapon>> weaponList(@RequestBody ItemWeapon entity) {
        entity.clearEmptyString();
        IPage<ItemWeapon> page = itemWeaponService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/weapon/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> weaponUpdate(@RequestBody ItemWeapon entity) {
        entity.clearEmptyString();
        itemWeaponService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/weapon/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> weaponRemove(@RequestBody ItemWeapon entity) {
        itemWeaponService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 护甲 ----------

    @PostMapping("/armor/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemArmor>> armorList(@RequestBody ItemArmor entity) {
        entity.clearEmptyString();
        IPage<ItemArmor> page = itemArmorService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/armor/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> armorUpdate(@RequestBody ItemArmor entity) {
        entity.clearEmptyString();
        itemArmorService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/armor/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> armorRemove(@RequestBody ItemArmor entity) {
        itemArmorService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 护手 ----------

    @PostMapping("/gloves/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemGloves>> glovesList(@RequestBody ItemGloves entity) {
        entity.clearEmptyString();
        IPage<ItemGloves> page = itemGlovesService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/gloves/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> glovesUpdate(@RequestBody ItemGloves entity) {
        entity.clearEmptyString();
        itemGlovesService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/gloves/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> glovesRemove(@RequestBody ItemGloves entity) {
        itemGlovesService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 头盔 ----------

    @PostMapping("/helmet/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemHelmet>> helmetList(@RequestBody ItemHelmet entity) {
        entity.clearEmptyString();
        IPage<ItemHelmet> page = itemHelmetService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/helmet/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> helmetUpdate(@RequestBody ItemHelmet entity) {
        entity.clearEmptyString();
        itemHelmetService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/helmet/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> helmetRemove(@RequestBody ItemHelmet entity) {
        itemHelmetService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 饰品 ----------

    @PostMapping("/accessory/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemAccessory>> accessoryList(@RequestBody ItemAccessory entity) {
        entity.clearEmptyString();
        IPage<ItemAccessory> page = itemAccessoryService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/accessory/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> accessoryUpdate(@RequestBody ItemAccessory entity) {
        entity.clearEmptyString();
        itemAccessoryService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/accessory/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> accessoryRemove(@RequestBody ItemAccessory entity) {
        itemAccessoryService.removeById(entity.getId());
        return WxResult.success();
    }

    // ---------- 护腿 ----------

    @PostMapping("/legs/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<ItemLegs>> legsList(@RequestBody ItemLegs entity) {
        entity.clearEmptyString();
        IPage<ItemLegs> page = itemLegsService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/legs/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> legsUpdate(@RequestBody ItemLegs entity) {
        entity.clearEmptyString();
        itemLegsService.saveOrUpdate(entity);
        return WxResult.success();
    }

    @PostMapping("/legs/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> legsRemove(@RequestBody ItemLegs entity) {
        itemLegsService.removeById(entity.getId());
        return WxResult.success();
    }
}
