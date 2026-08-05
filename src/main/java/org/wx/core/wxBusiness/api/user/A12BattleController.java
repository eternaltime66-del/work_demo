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
import org.wx.core.wxBusiness.game.entity.vo.BattleResultVo;
import org.wx.core.wxBusiness.game.service.BattleService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

/**
 * 前端-战斗演算
 */
@RestController
@RequestMapping("/api/battle")
public class A12BattleController {

    @Resource
    private BattleService battleService;

    /**
     * 按当前布局 + 关卡怪物进行行动值战斗演算；胜利后掉落入仓库
     */
    @PostMapping("/fight")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<BattleResultVo> fight(@ParamCheck(msg = "关卡ID") String levelId) {
        return WxResult.success(battleService.fight(Wx.memberId(), levelId));
    }
}
