package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

import java.math.BigDecimal;

/**
 * 角色基础数值配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_role_base_stat")
@BizIdPrefix("RBS")
public class RoleBaseStat extends WxBaseEntity<RoleBaseStat> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 配置名称 */
    private String name;

    /** 基础攻击力 */
    private Integer baseAtk;

    /** 基础生命值 */
    private Integer baseHp;

    /** 基础防御力 */
    private Integer baseDef;

    /** 基础行动值（由基础攻速反推：行动值 = 100 / 攻速） */
    private Integer baseAction;

    /** 基础攻速（默认 1；100 行动值 = 1 攻速） */
    private BigDecimal baseAtkSpeed;

    /** 额外生命值 */
    private Integer extraHp;

    /** 额外攻击力 */
    private Integer extraAtk;

    /** 额外防御力 */
    private Integer extraDef;

    /** 造成伤害比例（单位 1%，默认 100） */
    private BigDecimal dealDmgRatio;

    /** 受到伤害比例（单位 1%，默认 100） */
    private BigDecimal takenDmgRatio;

    /** 吸血比例（单位 1%，默认 0） */
    private BigDecimal lifeStealRatio;

    /** 增加攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedUpRatio;

    /** 减少攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedDownRatio;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 玩家默认拥有 */
    private Boolean defaultOwn;

    /** 是否主角 */
    private Boolean mainRole;
}
