package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 关卡类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_stage_type")
@BizIdPrefix("STY")
public class StageType extends WxBaseEntity<StageType> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;
    private String code;
    private Integer sort;
    private Boolean enable;
    private String remark;
}
