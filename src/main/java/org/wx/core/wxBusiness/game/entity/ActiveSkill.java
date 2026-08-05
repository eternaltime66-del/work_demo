package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.entity.enums.NeedChargeMode;

/**
 * 主动技能主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_active_skill")
@BizIdPrefix("ASK")
public class ActiveSkill extends WxBaseEntity<ActiveSkill> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 技能名称 */
    private String name;

    /** 技能分类：普攻 / 大招 / 小技能 */
    private ActiveSkillType skillType;

    /** 技能编码（便于扩展引用） */
    private String code;

    /**
     * 所需充能取值方式：
     * SELF_BASE_ACTION = 自己的基础行动值；
     * MANUAL = 使用 needCharge 手动值
     */
    private NeedChargeMode needChargeMode;

    /** 所需充能（needChargeMode=MANUAL 时生效） */
    private Integer needCharge;

    /** 本技能最大释放次数（0 表示不限） */
    private Integer maxCastSkill;

    /** 全局最大释放次数（0 表示不限） */
    private Integer maxCastGlobal;

    /** 所有手段最大释放次数（0 表示不限） */
    private Integer maxCastAllMeans;

    /** 本角色最大释放次数（0 表示不限） */
    private Integer maxCastRole;

    /** 排序 */
    private Integer sort;

    /** 是否启用 */
    private Boolean enable;

    /** 备注 */
    private String remark;

    /** 扩展 JSON */
    private String more;

    /**
     * 公式编辑器 token JSON 数组。
     * 例：[{"kind":"PARAM","paramMode":"READ","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"2"}]
     */
    private String formulaJson;
}
