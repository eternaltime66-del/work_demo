package org.wx.core.wxBusiness.api.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

/**
 * 前端-用户账户注册&登录
 */
@RestController
@RequestMapping("/api/account")
public class A1AccountController {

    /**
     * 邮箱注册
     */
    @PostMapping("/email/register")
    @WxRequestLog()
    public WxResult<String> emailRegister(
            @ParamCheck(msg = "邮箱") String email,
            @ParamCheck(msg = "验证码") String emsCode,
            @ParamCheck(msg = "密码") String psd,
            @ParamCheck(msg = "确认密码") String psdAgain
    ) {
        String token = Wx.MemberService.signUpEmailAccountForPsd(email, emsCode, psd, psdAgain);
        return WxResult.token(token);
    }

    /**
     * 邮箱密码登录
     */
    @PostMapping("/email/login")
    @WxRequestLog()
    public WxResult<String> emailLogin(
            @ParamCheck(msg = "邮箱") String email,
            @ParamCheck(msg = "密码") String password
    ) {
        String token = Wx.MemberService.signInEmailAccountForPsd(email, password);
        return WxResult.token(token);
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/email/login/code")
    @WxRequestLog()
    public WxResult<String> emailLoginByCode(
            @ParamCheck(msg = "邮箱") String email,
            @ParamCheck(msg = "验证码") String emsCode
    ) {
        String token = Wx.MemberService.signInEmailAccountForeMms(email, emsCode);
        return WxResult.token(token);
    }

    /**
     * 登录后修改密码（原密码）
     */
    @PostMapping("/password/change")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<?> changePassword(
            @ParamCheck(msg = "原密码") String oldPassword,
            @ParamCheck(msg = "新密码") String newPassword,
            @ParamCheck(msg = "确认密码") String newPasswordAgain
    ) {
        Wx.MemberService.changePassword(oldPassword, newPassword, newPasswordAgain);
        return WxResult.success();
    }

    /**
     * 登录后用邮箱验证码设置密码（无感注册后）
     */
    @PostMapping("/password/set-by-code")
    @WxRequestLog()
    @NeedHeader(roles = MemberRole.USER)
    public WxResult<?> setPasswordByCode(
            @ParamCheck(msg = "验证码") String emsCode,
            @ParamCheck(msg = "新密码") String newPassword,
            @ParamCheck(msg = "确认密码") String newPasswordAgain
    ) {
        Wx.MemberService.setPasswordByEmailCode(emsCode, newPassword, newPasswordAgain);
        return WxResult.success();
    }
}
