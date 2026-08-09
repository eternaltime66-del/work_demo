package org.wx.core.wxBusiness.account.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBase.unit.WordUnit;
import org.wx.core.wxBusiness.account.entity.Member;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.account.entity.enums.PointCoin;
import org.wx.core.wxBusiness.account.mapper.MemberMapper;
import org.wx.core.wxBusiness.code.CodeEnum;
import org.wx.core.wxBusiness.game.service.PlayerRoleService;
import org.wx.core.wxBusiness.game.service.WarehouseService;

/**
 * Member Service实现类
 *
 * @author 无心
 * @date 2026-01-16
 */
@Service
public class MemberService extends WxServiceImpl<MemberMapper, Member> {

    @Resource
    private PlayerRoleService playerRoleService;
    @Resource
    private WarehouseService warehouseService;

    @Transactional(rollbackFor = Exception.class)
    public void initUser(String uid) {
        Member member = new Member();
        Member oldMember = this.getById(uid);
        if (oldMember == null) {
            member.setId(uid);
            member.setMemberRole(MemberRole.USER);
            this.save(member);
            Wx.PointWalletService.getSysPointWallet(uid, PointCoin.USDT);
            playerRoleService.grantDefaultRoles(uid);
            warehouseService.ensureWarehouse(uid);
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
        playerRoleService.grantDefaultRoles(member.getId());
        warehouseService.ensureWarehouse(member.getId());
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
        Wx.CodeFactory.checkCode(emsCode, email, codeEnum);
        Wx.CodeFactory.delCode(email, codeEnum);
        Member member = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.USER)
                .one();
        if (member == null) {
            // 无感注册：验证码通过且邮箱未注册 → 自动建号并登录
            String randomPsd = WordUnit.randomKey(12, 2);
            member = addUser(email, randomPsd, randomPsd);
            member.setMemberRole(MemberRole.USER);
            this.wxUpdateById(member, Member::getMemberRole);
            Wx.PointWalletService.getSysPointWallet(member.getId(), PointCoin.USDT);
            playerRoleService.grantDefaultRoles(member.getId());
            warehouseService.ensureWarehouse(member.getId());
            return member.getToken();
        }
        playerRoleService.ensureMainRole(member.getId());
        warehouseService.ensureWarehouse(member.getId());
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
        playerRoleService.ensureMainRole(member.getId());
        warehouseService.ensureWarehouse(member.getId());
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

    /** 当前登录用户修改密码（需原密码） */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword, String newPasswordAgain) {
        ErrorFactory.notEmpty(oldPassword, "请输入原密码");
        ErrorFactory.notEmpty(newPassword, "请输入新密码");
        ErrorFactory.notEmpty(newPasswordAgain, "请确认新密码");
        ErrorFactory.notEquals(newPassword, newPasswordAgain, "两次新密码不一致");
        ErrorFactory.throwError(newPassword.equals(oldPassword), "新密码不能与原密码相同");
        ErrorFactory.throwError(newPassword.length() < 6, "新密码至少 6 位");

        Member member = this.getById(Wx.memberId());
        ErrorFactory.throwError(member == null, "用户不存在");
        member.verifyPsd(oldPassword);
        member.setPassword(member.psdEncode(newPassword));
        this.wxUpdateById(member, Member::getPassword);
    }

    /** 当前登录用户：邮箱验证码设置/重置密码（无感注册后可用） */
    @Transactional(rollbackFor = Exception.class)
    public void setPasswordByEmailCode(String emsCode, String newPassword, String newPasswordAgain) {
        ErrorFactory.notEmpty(emsCode, "请输入验证码");
        ErrorFactory.notEmpty(newPassword, "请输入新密码");
        ErrorFactory.notEmpty(newPasswordAgain, "请确认新密码");
        ErrorFactory.notEquals(newPassword, newPasswordAgain, "两次新密码不一致");
        ErrorFactory.throwError(newPassword.length() < 6, "新密码至少 6 位");

        Member member = this.getById(Wx.memberId());
        ErrorFactory.throwError(member == null, "用户不存在");
        ErrorFactory.notEmpty(member.getEmail(), "账号未绑定邮箱");
        String email = member.getEmail().trim().toLowerCase();
        CodeEnum codeEnum = CodeEnum.AccountCheckForEmail;
        Wx.CodeFactory.checkCode(emsCode, email, codeEnum);
        Wx.CodeFactory.delCode(email, codeEnum);
        member.setPassword(member.psdEncode(newPassword));
        this.wxUpdateById(member, Member::getPassword);
    }

    @Transactional(rollbackFor = Exception.class)
    public String signUpAdminAccountForPsd(
            String email,
            String emsCode,
            String psd,
            String psdAgain
    ) {
        email = email.trim().toLowerCase();
        CodeEnum codeEnum = CodeEnum.AccountCheckForEmail;
        Wx.CodeFactory.checkCode(emsCode, email, codeEnum);
        Wx.CodeFactory.delCode(email, codeEnum);
        Member userAccount = this.find()
                .eq(Member::getEmail, email)
                .eq(Member::getMemberRole, MemberRole.ADMIN)
                .one();
        ErrorFactory.throwError(userAccount != null, "管理员已注册");
        Member member = addUser(email, psd, psdAgain);
        member.setMemberRole(MemberRole.ADMIN);
        this.wxUpdateById(member, Member::getMemberRole);
        return member.getToken();
    }

}
