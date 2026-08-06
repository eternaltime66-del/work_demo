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
    private BigDecimal weight;
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
    private Integer anchorPassiveSlotCount;
    private Integer playerDefaultEditAnchorPassiveSlotCount;
    private Integer playerMaxEditAnchorPassiveSlotCount;
    private Integer periodicPassiveSlotCount;
    private Integer playerDefaultEditPeriodicPassiveSlotCount;
    private Integer playerMaxEditPeriodicPassiveSlotCount;
    private Integer sustainedPassiveSlotCount;
    private Integer playerDefaultEditSustainedPassiveSlotCount;
    private Integer playerMaxEditSustainedPassiveSlotCount;

    /** 武器 */
    private Integer baseAtk;
    private BigDecimal atkSpeedUpRatio;
    private BigDecimal atkSpeedDownRatio;
    private String normalSkillId;
    private String normalSkillName;

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

    /** 默认锚点被动描述 */
    private List<PassiveDescVo> defaultAnchorPassiveDescs = new ArrayList<>();

    /** 默认周期被动描述 */
    private List<PassiveDescVo> defaultPeriodicPassiveDescs = new ArrayList<>();

    /** 默认持续效果被动描述 */
    private List<PassiveDescVo> defaultSustainedPassiveDescs = new ArrayList<>();
}
