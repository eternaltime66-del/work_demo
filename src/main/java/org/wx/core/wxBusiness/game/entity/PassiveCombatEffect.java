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
 * 战斗内被动效果（锚点/周期），结构对齐主动技能效果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_passive_combat_effect")
@BizIdPrefix("PCE")
public class PassiveCombatEffect extends WxBaseEntity<PassiveCombatEffect> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 被动技能 id */
    private String skillId;

    private String name;

    private SkillEffectTarget targetType;

    private SkillEffectType effectType;

    private AttrModifyKey attrKey;

    private AttrModifyDirection attrDir;

    private String formulaJson;

    private Integer hitSegments;

    /**
     * 本条效果触发概率（百分比整数）。
     * 100 = 100% 必触发；未命中则跳过该效果。
     */
    private Integer triggerRate;

    /**
     * ATTR_MODIFY 生效行动值；0/null=永久。
     * 持续效果被动不使用本字段（由条件维持）。
     */
    private Integer durationAv;

    private Integer sort;

    private String remark;

    private String more;
}
