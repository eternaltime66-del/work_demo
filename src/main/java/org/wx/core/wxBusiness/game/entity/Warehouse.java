package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 玩家仓库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_warehouse")
@BizIdPrefix("WH")
public class Warehouse extends WxBaseEntity<Warehouse> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 玩家 uid */
    private String uid;

    /** 最大格子数 */
    private Integer maxSlots;
}
