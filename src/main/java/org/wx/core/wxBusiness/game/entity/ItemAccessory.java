package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 物品-饰品扩展（无面板攻速；属性走被动等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_accessory")
@BizIdPrefix("ACC")
public class ItemAccessory extends WxBaseEntity<ItemAccessory> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品主表 id */
    private String itemId;

    private String remark;
}
