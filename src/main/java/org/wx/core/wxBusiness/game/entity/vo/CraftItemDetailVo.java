package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 合成详情里展示的产出物品属性
 */
@Data
public class CraftItemDetailVo {

    private String itemId;
    private String code;
    private String name;
    private String icon;
    private String itemType;
    private Integer maxStack;
    private String remark;

    /** 默认充能技能槽数量 */
    private Integer chargeSkillSlotCount;
    private Integer playerDefaultEditChargeSkillSlotCount;
    private Integer playerMaxEditChargeSkillSlotCount;
    private Integer basicPassiveSlotCount;
    private Integer playerDefaultEditBasicPassiveSlotCount;
    private Integer playerMaxEditBasicPassiveSlotCount;
    private Integer advancedPassiveSlotCount;
    private Integer playerDefaultEditAdvancedPassiveSlotCount;
    private Integer playerMaxEditAdvancedPassiveSlotCount;

    /** 武器 */
    private Integer baseAtk;
    private BigDecimal atkSpeedUpRatio;
    private BigDecimal atkSpeedDownRatio;
    private String normalSkillId;
    private String normalSkillName;
    /** true=武器未绑普攻，展示的是系统 DEFAULT_NORMAL */
    private Boolean normalSkillSystemDefault;

    /** 防具类 */
    private Integer hp;
    private Integer defense;

    /** 材料 */
    private Integer grade;

    /** 默认充能技能名（兼容旧展示） */
    private List<String> defaultSkillNames = new ArrayList<>();

    /** 默认充能技能（含 id，供前端点击查看详情） */
    private List<SkillLinkVo> defaultSkills = new ArrayList<>();

    /** 类型中文 */
    private String itemTypeLabel;

    /** 攻速文案（武器/饰品） */
    private String atkSpeedText;

    /** 普攻技能完整描述 */
    private SkillDescVo normalSkillDesc;

    /** 默认充能技能完整描述 */
    private List<SkillDescVo> defaultSkillDescs = new ArrayList<>();

    /** 默认基础属性被动描述 */
    private List<PassiveDescVo> defaultBasicPassiveDescs = new ArrayList<>();

    /** 默认高级属性被动描述 */
    private List<PassiveDescVo> defaultAdvancedPassiveDescs = new ArrayList<>();

    /** 战斗锚点：开战 / 判定 / 脉冲 / 战斗事件 */
    private List<PassiveDescVo> defaultBattleStartPassiveDescs = new ArrayList<>();
    private List<PassiveDescVo> defaultBattleJudgePassiveDescs = new ArrayList<>();
    private List<PassiveDescVo> defaultBattlePulsePassiveDescs = new ArrayList<>();
    private List<PassiveDescVo> defaultBattleCombatPassiveDescs = new ArrayList<>();
}
