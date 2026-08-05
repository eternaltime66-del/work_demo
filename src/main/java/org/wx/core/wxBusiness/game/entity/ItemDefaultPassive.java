package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;

/**
 * 装备默认被动（基础属性 / 高级属性）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_default_passive")
@BizIdPrefix("IDP")
public class ItemDefaultPassive extends WxBaseEntity<ItemDefaultPassive> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String itemId;

    private String passiveSkillId;

    /** OUT_BASIC / OUT_ADVANCED / IN_ANCHOR */
    private PassiveSkillType passiveType;

    private Integer slotNo;

    private Integer sort;

    @TableField(exist = false)
    private String passiveSkillName;
}
