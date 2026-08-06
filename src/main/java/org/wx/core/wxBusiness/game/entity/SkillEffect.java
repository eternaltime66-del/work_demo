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
 * 主动技能-效果配置（1 技能可多条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_skill_effect")
@BizIdPrefix("SEF")
public class SkillEffect extends WxBaseEntity<SkillEffect> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 主动技能 id */
    private String skillId;

    /** 效果配置名 */
    private String name;

    /** 目标选择 */
    private SkillEffectTarget targetType;

    /** 触发效果类型 */
    private SkillEffectType effectType;

    /** ATTR_MODIFY：修改属性（攻击/最大生命/防御） */
    private AttrModifyKey attrKey;

    /** ATTR_MODIFY：增加 / 减少 */
    private AttrModifyDirection attrDir;

    /**
     * 伤害/治疗/属性修改公式 token JSON。
     * 读取参数可带 readRole：SELF / GLOBAL / EACH_TARGET（EACH_TARGET 仅效果公式）
     */
    private String formulaJson;

    /** 本公式触发几段（伤害/治疗/属性修改） */
    private Integer hitSegments;

    /**
     * 本条效果触发概率（百分比整数）。
     * 100 = 100% 必触发；释放技能时对每条效果独立掷骰，未命中则跳过该效果。
     */
    private Integer triggerRate;

    /**
     * ATTR_MODIFY 生效行动值；0/null=永久修改。
     * &gt;0 时挂时长 buff，经过该行动值后撤销；同源再触发刷新时长不叠层。
     */
    private Integer durationAv;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 扩展 JSON */
    private String more;
}
