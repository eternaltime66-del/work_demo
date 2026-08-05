package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

/**
 * 快捷充能组（可复用模板，细节后续补充）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_skill_charge_group")
@BizIdPrefix("SCG")
public class SkillChargeGroup extends WxBaseEntity<SkillChargeGroup> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private Integer sort;

    private Boolean enable;

    private String remark;

    private String more;
}
