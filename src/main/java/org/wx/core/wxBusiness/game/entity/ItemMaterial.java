package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 物品-材料扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_material")
@BizIdPrefix("MAT")
public class ItemMaterial extends WxBaseEntity<ItemMaterial> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品主表 id */
    private String itemId;

    /** 品级 */
    private Integer grade;

    private String remark;
}
