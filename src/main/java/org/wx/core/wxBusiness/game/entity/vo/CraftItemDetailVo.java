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

    /** 充能技能槽数量 */
    private Integer chargeSkillSlotCount;
    private Boolean playerCanEditSkillSlot;

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
}
