package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 小关
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_stage_level")
@BizIdPrefix("SLV")
public class StageLevel extends WxBaseEntity<StageLevel> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 大关 id */
    private String chapterId;

    private String name;
    /** 如 1-1 */
    private String code;
    private Integer sort;
    private Boolean enable;
    private String remark;
}
