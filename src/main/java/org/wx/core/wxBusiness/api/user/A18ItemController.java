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
import org.wx.core.wxBusiness.game.entity.vo.CraftItemDetailVo;
import org.wx.core.wxBusiness.game.entity.Item;
import org.wx.core.wxBusiness.game.service.ItemDetailService;
import org.wx.core.wxBusiness.game.service.ItemService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-物品详情（预览奖励 / 掉落等只读查看）
 */
@RestController
@RequestMapping("/api/item")
public class A18ItemController {

    @Resource
    private ItemDetailService itemDetailService;

    @Resource
    private ItemService itemService;

    @PostMapping("/detail")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<CraftItemDetailVo> detail(@ParamCheck(msg = "物品ID") String itemId) {
        CraftItemDetailVo vo = itemDetailService.buildRichByItemId(itemId);
        ErrorFactory.throwError(vo == null, "物品不存在");
        return WxResult.success(vo);
    }
    /** 玩家图鉴：包含全部启用物品，以及尚无获取途径的预览物品。 */
    @PostMapping("/compendium")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<Item>> compendium() {
        List<Item> rows = itemService.find()
                .eq(org.wx.core.wxBusiness.game.entity.Item::getEnable, true)
                .orderByAsc(org.wx.core.wxBusiness.game.entity.Item::getSort)
                .list();
        return WxResult.success(rows);
    }
}
