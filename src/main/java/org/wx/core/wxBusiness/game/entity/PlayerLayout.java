package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 玩家布阵布局（己方 横6*竖5）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_layout")
@BizIdPrefix("PLY")
public class PlayerLayout extends WxBaseEntity<PlayerLayout> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;

    /** 玩家角色 id */
    private String roleId;

    /** 横坐标 0-4（左上角锚点） */
    private Integer posCol;

    /** 纵坐标 0-3（左上角锚点） */
    private Integer posRow;

    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private Boolean mainRole;

    @TableField(exist = false)
    private Integer gridH;

    @TableField(exist = false)
    private Integer gridW;

    @TableField(exist = false)
    private Integer baseHp;

    @TableField(exist = false)
    private Integer extraHp;
}
