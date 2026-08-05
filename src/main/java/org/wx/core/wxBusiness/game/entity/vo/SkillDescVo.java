package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 主动技能人性化描述
 */
@Data
public class SkillDescVo {
    private String id;
    private String name;
    private String skillType;
    private String skillTypeLabel;
    private String needChargeText;
    private String castLimitText;
    private List<String> chargeTexts = new ArrayList<>();
    private List<String> effectTexts = new ArrayList<>();
    /** 多行汇总文案 */
    private String summary;
}
