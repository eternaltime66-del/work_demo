package org.wx.core.wxBusiness.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.Member;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.account.entity.enums.PointCoin;
import org.wx.core.wxBusiness.account.mapper.MemberMapper;
import org.wx.core.wxBusiness.code.CodeEnum;

/**
 * Member Service实现类
 *
 * @author 无心
 * @date 2026-01-16
 */
@Service
public class MemberService extends WxServiceImpl<MemberMapper, Member> {

    @Transactional(rollbackFor = Exception.class)
    public void initUser(String uid) {
        Member member = new Member();
        Member oldMember = this.getById(uid);
        if (oldMember == null) {
            member.setId(uid);
            member.setMemberRole(MemberRole.USER);
            this.save(member);
            Wx.PointWalletService.getSysPointWallet(uid, PointCoin.USDT);
        }
    }

    /**
     * 注册用户 公共方法
     */
    @Transactional(rollbackFor = Exception.class)
    public Member addUser(
            String email,
            String psd,
            String psdAgain
    ) {
        Member member = Member.commonMember();
        boolean isEmptyPsd = Wx.isEmpty(psd) || Wx.isEmpty(psdAgain);
        if (isEmptyPsd) {
            psd = "123456";
            psdAgain = "123456";
        }
        ErrorFactory.notEquals(psd, psdAgain, "两次密码不一致");
        member.setEmail(email);
        member.setPassword(member.psdEncode(psd));
        member.setToken(Member.creteToken());
        this.save(member);
        Wx.RedisFactory.setBuyDay(member.getToken(), member.getId(), 7);

        return member;
    }

    @Transactional(rollbackFor = Exception.class)
    public String signUpEmailAccountForPsd(
            String email,
            String emsCode,
            String psd,
            String psdAgain
    ) {
        email = email.trim().toLowerCase();
        CodeEnum codeEnum = CodeEnum.AccountCheckForEmail;
        String account = email;
        String code = emsCode;
        Wx.CodeFactory.checkCode(code, account, codeEnum);
        Wx.CodeFactory.delCode(account, codeEnum);
        Member userAccount = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.USER)
                .one();
        ErrorFactory.throwError(userAccount != null, "用户已注册");
        Member member = addUser(email, psd, psdAgain);
        member.setMemberRole(MemberRole.USER);
        this.wxUpdateById(member);
        Wx.PointWalletService.getSysPointWallet(member.getId(), PointCoin.USDT);
        return member.getToken();
    }

    /*-----------登录-邮箱账户-邮箱验证码----------*/

    @Transactional(rollbackFor = Exception.class)
    public String signInEmailAccountForeMms(
            String email,
            String emsCode
    ) {
        email = email.trim().toLowerCase();
        CodeEnum codeEnum = CodeEnum.AccountCheckForEmail;
        String account = email;
        String code = emsCode;
        Wx.CodeFactory.checkCode(code, account, codeEnum);
        Wx.CodeFactory.delCode(account, codeEnum);
        Member member = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.USER)
                .one();
        ErrorFactory.throwError(member == null, "用户未注册");
        member.setToken(Member.creteToken());
        Wx.RedisFactory.setBuyDay(member.getToken(), member.getId(), 7);
        return member.getToken();
    }

    @Transactional(rollbackFor = Exception.class)
    public void forgetEmailAccountForEms(
            String email,
            String emsCode,
            String psd,
            String psdAgain
    ) {
        email = email.trim().toLowerCase();
        CodeEnum codeEnum = CodeEnum.AccountCheckForEmail;
        String account = email;
        String code = emsCode;

        Member member = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.USER)
                .one();
        ErrorFactory.notEquals(psd, psdAgain, "两次密码不一致");
        ErrorFactory.throwError(member == null, "用户未注册");
        Wx.CodeFactory.checkCode(code, account, codeEnum);
        Wx.CodeFactory.delCode(account, codeEnum);
        member.setPassword(member.psdEncode(psd));
        this.wxUpdateById(member, Member::getPassword);
    }


    /*-----------手机号-邮箱验证码-登录账户----------*/
    @Transactional(rollbackFor = Exception.class)
    public String signInEmailAccountForPsd(
            String email,
            String password
    ) {
        Member member = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.USER)
                .one();
        ErrorFactory.throwError(member == null, "用户未注册");
        member.verifyPsd(password);
        member.setToken(Member.creteToken());
        Wx.RedisFactory.setBuyDay(member.getToken(), member.getId(), 7);
        return member.getToken();
    }

    @Transactional(rollbackFor = Exception.class)
    public String superToken(String uid) {
        Member member = getById(uid);
        ErrorFactory.throwError(member == null, "用户不存在");
        member.setToken(Member.creteToken());
        Wx.RedisFactory.setBuyDay(member.getToken(), member.getId(), 7);
        return member.getToken();
    }

    @Transactional(rollbackFor = Exception.class)
    public String signInAdminForPsd(
            String email,
            String password
    ) {
        Member member = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.ADMIN)
                .one();
        ErrorFactory.throwError(member == null, "用户未注册");
        member.verifyPsd(password);
        member.setToken(Member.creteToken());
        Wx.RedisFactory.setBuyDay(member.getToken(), member.getId(), 7);
        return member.getToken();
    }

}
