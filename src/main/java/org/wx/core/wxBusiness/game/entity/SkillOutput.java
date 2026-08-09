package org.wx.core.wxBusiness.game.entity;



import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import lombok.EqualsAndHashCode;

import org.wx.core.wxBase.annotation.BizIdPrefix;

import org.wx.core.wxBase.base.WxBaseEntity;

import org.wx.core.wxBusiness.game.entity.enums.AttrModifyDirection;

import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;

import org.wx.core.wxBusiness.game.entity.enums.DamageElement;

import org.wx.core.wxBusiness.game.entity.enums.SkillEffectTarget;

import org.wx.core.wxBusiness.game.entity.enums.SkillEffectType;

import org.wx.core.wxBusiness.game.entity.enums.SkillOutputKind;



/**

 * 最终输出模块（主动 skillId / 被动 passiveSkillId 二选一）。

 */

@Data

@EqualsAndHashCode(callSuper = true)

@TableName("app_skill_output")

@BizIdPrefix("SOUT")

public class SkillOutput extends WxBaseEntity<SkillOutput> {



    @TableId(type = IdType.INPUT)

    private String id;



    private String skillId;



    private String passiveSkillId;



    private String name;



    private SkillOutputKind outputKind;



    private SkillEffectTarget targetType;



    /** ATTR */

    private AttrModifyKey attrKey;



    private AttrModifyDirection attrDir;



    /** EFFECT：DAMAGE / HEAL */

    private SkillEffectType effectType;



    private DamageElement damageElement;



    private String formulaJson;



    private Integer hitSegments;



    private Integer triggerRate;



    /** ATTR 持续行动值；0=战斗内永久（BUFF 持续在 BuffDef） */

    private Integer durationAv;



    /** APPEND_BUFF */

    private String buffDefId;



    /** 展示用：追加 BUFF 名称 */

    @TableField(exist = false)

    private String buffDefName;



    private Integer sort;



    private String remark;



    private String more;

}


