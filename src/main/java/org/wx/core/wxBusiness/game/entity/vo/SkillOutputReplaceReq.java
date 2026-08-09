package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.SkillOutput;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkillOutputReplaceReq {
    private String skillId;
    private List<SkillOutput> outputs = new ArrayList<>();
}
