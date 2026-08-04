package org.wx.core.wxBusiness.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBase.unit.WordUnit;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;

/**
 * Member 实体类
 *
 * @author 无心
 * @date 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_member")
public class Member extends WxBaseEntity<Member> {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 密码
     */
    private String password;

    /**
     * token
     */
    private String token;

    /**
     * 盐
     */
    private String salt;

    /**
     * 用户角色
     */
    private MemberRole memberRole;

    /**
     * 昵称
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 备注
     */
    private String remark;

    public static Member commonMember() {
        Member member = new Member();
        member.id = WordUnit.randomKey(10, 1);
        member.memberRole = MemberRole.USER;
        member.salt = WordUnit.randomKey(12, 2);
        return member;
    }

    public void verifyPsd(String password) {
        ErrorFactory.throwError(!this.password.equals(psdEncode(password)), "密码有误");
    }

    public String psdEncode(String password) {
        // 新增安全规则：双盐 + 双层加密
        String step1 = WordUnit.md5(this.id + password + this.salt);
        String s = WordUnit.md5(step1 + this.id);

        return s;
    }

    public static void main(String[] args) {
        String string = WordUnit.md5("SuperAdmin" + "llsw1229@" + "jrwJ");
        System.out.println(WordUnit.md5(string + "SuperAdmin"));
    }

    public static String creteToken() {
        return WordUnit.randomKey(12, 2).toUpperCase();
    }

    public void info() {
        this.salt = null;
        this.password = null;
    }

    @Data
    public static class UserMore {
    }

    @TableField(exist = false)
    private UserMore more = new UserMore();

    @TableField(exist = false)
    private Boolean myFollowUser;
}
