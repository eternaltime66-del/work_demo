package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.ItemDefaultSkill;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemDefaultSkillSaveReq {
    private String itemId;
    private List<ItemDefaultSkill> skills = new ArrayList<>();
}
