package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.PlayerRoleCategory;
import org.wx.core.wxBusiness.game.entity.vo.RoleSkillViewVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 玩家持有角色（绑定 uid）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_role")
@BizIdPrefix("PR")
public class PlayerRole extends WxBaseEntity<PlayerRole> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 玩家 uid */
    private String uid;

    /** 来源基础数值配置 id */
    private String baseStatId;

    /** 角色名称 */
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

    /** 造成伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal dealDmgRatio;

    /** 受到伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal takenDmgRatio;

    /** 造成元素伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal dealElementDmgRatio;

    /** 受到元素伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal takenElementDmgRatio;

    /** 造成物理伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal dealPhysDmgRatio;

    /** 受到物理伤害比例（单位 1%，默认 100；乘法叠乘） */
    private BigDecimal takenPhysDmgRatio;

    /** 吸血比例（单位 1%，默认 0） */
    private BigDecimal lifeStealRatio;

    /** 最终攻击比例（单位 1%，默认 100；总攻击 = 攻击合计 × 比例） */
    private BigDecimal finalAtkRatio;

    /** 最终生命比例（单位 1%，默认 100） */
    private BigDecimal finalHpRatio;

    /** 最终防御比例（单位 1%，默认 100） */
    private BigDecimal finalDefRatio;

    /** 增加攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedUpRatio;

    /** 减少攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedDownRatio;

    /** 是否主角（与 roleCategory=HERO 同步） */
    private Boolean mainRole;

    /** 角色分类：主角 / 伙伴 / 召唤物 */
    private PlayerRoleCategory roleCategory;

    /** 召唤物：基础攻击继承召唤者比例（单位 1%） */
    private BigDecimal inheritAtkRatio;

    /** 召唤物：基础防御继承召唤者比例（单位 1%） */
    private BigDecimal inheritDefRatio;

    /** 召唤物：基础生命继承召唤者比例（单位 1%） */
    private BigDecimal inheritHpRatio;

    /** 占地高度（主角默认2） */
    private Integer gridH;

    /** 占地宽度（主角默认2） */
    private Integer gridW;

    /** 展示用：含装备的最终攻击 */
    @TableField(exist = false)
    private Integer displayAtk;

    /** 展示用：含装备的最终生命 */
    @TableField(exist = false)
    private Integer displayHp;

    /** 展示用：含装备的最终防御 */
    @TableField(exist = false)
    private Integer displayDef;

    /** 展示用：含攻速叠乘的最终行动值（越小越快） */
    @TableField(exist = false)
    private Integer displayAction;

    /** 展示用：含装备后的最终攻速（= 100 / displayAction） */
    @TableField(exist = false)
    private BigDecimal displayAtkSpeed;

    /** 装备提供的攻击加成 */
    @TableField(exist = false)
    private Integer equipBonusAtk;

    /** 装备提供的生命加成 */
    @TableField(exist = false)
    private Integer equipBonusHp;

    /** 装备提供的防御加成 */
    @TableField(exist = false)
    private Integer equipBonusDef;

    /** 详情展示：全部技能一览（角色绑定 + 武器普攻 + 装备充能） */
    @TableField(exist = false)
    private List<RoleSkillViewVo> skills;
}
