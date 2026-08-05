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
 * 装备默认充能技能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_default_skill")
@BizIdPrefix("IDS")
public class ItemDefaultSkill extends WxBaseEntity<ItemDefaultSkill> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品主表 id */
    private String itemId;

    /** 主动技能 id */
    private String skillId;

    /** 槽位序号（从 0 开始） */
    private Integer slotNo;

    private Integer sort;

    @TableField(exist = false)
    private String skillName;

    @TableField(exist = false)
    private String skillType;
}
