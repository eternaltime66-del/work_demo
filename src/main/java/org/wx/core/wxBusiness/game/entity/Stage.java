package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.StageKind;

/**
 * 关卡树节点（单表：分类 / 大关 / 小关）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_stage")
@BizIdPrefix("STG")
public class Stage extends WxBaseEntity<Stage> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 父节点 id；根（TYPE）为 null */
    private String parentId;

    private StageKind kind;

    private String name;
    private String code;
    private Integer sort;
    private Boolean enable;
    private String remark;

    /** 已首通后每次消耗体力；未首通固定 0 */
    private Integer staminaCost;
    /** 每天最大攻打次数；null=无限（按 attempt_date 自然日重置） */
    private Integer dailyMaxAttempts;

    /** 兼容旧前端：大关提交 typeId → parentId */
    @TableField(exist = false)
    private String typeId;

    /** 兼容旧前端：小关提交 chapterId → parentId */
    @TableField(exist = false)
    private String chapterId;
}
