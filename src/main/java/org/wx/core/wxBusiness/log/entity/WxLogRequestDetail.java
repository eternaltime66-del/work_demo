package org.wx.core.wxBusiness.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * WxLogRequestDetail 实体类
 * @author 无心
 * @date 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_log_request_detail")
@BizIdPrefix("LRD")
public class WxLogRequestDetail extends WxBaseEntity<WxLogRequestDetail> {

    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 关联L1请求ID
     */
    private String requestId;

    /**
     * 操作表名
     */
    private String tableName;

    /**
     * INSERT / UPDATE / DELETE
     */
    private String actionType;

    /**
     * 变更前数据
     */
    private String beforeData;

    /**
     * 变更后数据
     */
    private String afterData;

    /**
     * 变动的数据
     */
    private String changeData;

    /**
     * 操作人ID
     */
    private String operatorId;

}
