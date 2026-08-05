package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 被动技能人性化描述
 */
@Data
public class PassiveDescVo {
    private String id;
    private String name;
    private String passiveType;
    private String passiveTypeLabel;
    private String conditionText;
    private List<String> conditionTexts = new ArrayList<>();
    private List<String> effectTexts = new ArrayList<>();
    /** 多行汇总文案 */
    private String summary;
}
