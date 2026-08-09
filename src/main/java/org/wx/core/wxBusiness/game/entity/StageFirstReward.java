package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_stage_first_reward")
@BizIdPrefix("SFR")
public class StageFirstReward extends WxBaseEntity<StageFirstReward> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** CHAPTER 或 LEVEL */
    private String stageId;
    private String itemId;
    private Integer qty;
    private Integer sort;

    @TableField(exist = false)
    private String itemName;
}
