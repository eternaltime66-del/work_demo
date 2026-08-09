package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.MonsterRarity;

/**
 * 关卡怪物与位置配置（敌方 横6*竖5）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_stage_level_monster")
@BizIdPrefix("SLM")
public class StageLevelMonster extends WxBaseEntity<StageLevelMonster> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 小关 id */
    private String levelId;

    /** 怪物 id */
    private String monsterId;

    /** 横坐标 0-4（左上角锚点） */
    private Integer posCol;

    /** 纵坐标 0-3（左上角锚点） */
    private Integer posRow;

    private Integer sort;
    private String remark;

    @TableField(exist = false)
    private String monsterName;

    @TableField(exist = false)
    private MonsterRarity rarity;

    @TableField(exist = false)
    private Integer gridH;

    @TableField(exist = false)
    private Integer gridW;

    @TableField(exist = false)
    private Integer baseHp;

    @TableField(exist = false)
    private Integer baseAtk;

    @TableField(exist = false)
    private Integer baseDef;

    @TableField(exist = false)
    private Integer baseAction;

    @TableField(exist = false)
    private String normalSkillId;

    @TableField(exist = false)
    private String normalSkillName;

    @TableField(exist = false)
    private String smallSkillId;

    @TableField(exist = false)
    private String smallSkillName;

    @TableField(exist = false)
    private String ultimateSkillId;

    @TableField(exist = false)
    private String ultimateSkillName;

    @TableField(exist = false)
    private String remarkText;
}
