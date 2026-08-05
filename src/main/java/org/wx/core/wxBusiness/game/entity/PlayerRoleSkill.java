package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 玩家角色持有技能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_role_skill")
@BizIdPrefix("PRS")
public class PlayerRoleSkill extends WxBaseEntity<PlayerRoleSkill> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 玩家角色 id */
    private String roleId;

    /** 主动技能 id */
    private String skillId;

    /** 排序 */
    private Integer sort;
}
