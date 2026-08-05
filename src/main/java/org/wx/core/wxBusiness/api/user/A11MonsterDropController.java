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
import org.wx.core.wxBusiness.game.entity.vo.MonsterDropResultVo;
import org.wx.core.wxBusiness.game.service.MonsterDropService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前端-怪物掉落结算（独立概率 + 非线性数量）
 */
@RestController
@RequestMapping("/api/monster/drop")
public class A11MonsterDropController {

    @Resource
    private MonsterDropService monsterDropService;

    /**
     * 仅试算掉落（不入仓）
     */
    @PostMapping("/roll")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<MonsterDropResultVo>> roll(@ParamCheck(msg = "怪物ID") String monsterIds) {
        return WxResult.success(monsterDropService.rollDrops(parseIds(monsterIds)));
    }

    /**
     * 结算掉落并发放到仓库
     */
    @PostMapping("/settle")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<MonsterDropResultVo>> settle(@ParamCheck(msg = "怪物ID") String monsterIds) {
        return WxResult.success(monsterDropService.rollAndGrantToWarehouse(Wx.memberId(), parseIds(monsterIds)));
    }

    private List<String> parseIds(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
