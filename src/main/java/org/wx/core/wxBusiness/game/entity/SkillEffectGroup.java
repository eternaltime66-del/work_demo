package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;
import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;

/**
 * 快捷效果组（可复用模板）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_skill_effect_group")
@BizIdPrefix("SEG")
public class SkillEffectGroup extends WxBaseEntity<SkillEffectGroup> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    /** 目标选择 */
    private SkillEffectTarget targetType;

    /** 触发效果类型 */
    private SkillEffectType effectType;

    /** ATTR_MODIFY：修改属性 */
    private AttrModifyKey attrKey;

    /** ATTR_MODIFY：增加 / 减少 */
    private AttrModifyDirection attrDir;

    /** 伤害/治疗/属性修改公式 token JSON */
    private String formulaJson;

    /** 本公式触发几段 */
    private Integer hitSegments;

    private Integer sort;

    private Boolean enable;

    private String remark;

    private String more;
}
