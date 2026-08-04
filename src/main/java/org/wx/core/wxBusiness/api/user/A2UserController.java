package org.wx.core.wxBusiness.api.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.Member;
import org.wx.core.wxBusiness.account.entity.PointWallet;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.account.entity.enums.PointCoin;
import org.wx.core.wxBusiness.account.service.PointWalletService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 前端-用户中心(登录后)
 */
@RestController
@RequestMapping("/api/user")
public class A2UserController {

    /**
     * 超级登录（需 superKey 校验）
     */
    @PostMapping("/super/token")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    public WxResult<Object> superToken(
            @ParamCheck String superKey,
            @ParamCheck String uid
    ) {
        ErrorFactory.throwError(!"9527".equals(superKey), "superKey无效");
        return WxResult.success(Wx.MemberService.superToken(uid));
    }

    /**
     * 用户详情
     */
    @PostMapping("/info")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<Member> info(
    ) {
        Member member = Wx.member();
        member.info();
        return WxResult.success(member);
    }

    @Resource
    public PointWalletService pointWalletService;

    /**
     * 查余额
     */
    @PostMapping("/wallet/balance")
    @WxRequestLog(recordRequest = false, recordResponse = false)
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<List<PointWallet>> walletBalance(
            @ParamCheck PointWallet entity
    ) {

        IPage<PointWallet> page = pointWalletService.find().eq(PointWallet::getUid, Wx.memberId()).page();
        if (page.getRecords().size() != PointCoin.values().length) {
            pointWalletService.init(Wx.memberId());
            page = pointWalletService.find().eq(PointWallet::getUid, Wx.memberId()).page();
        }
        return WxResult.page(page);
    }
}
