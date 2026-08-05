package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.ItemDefaultPassive;
import org.wx.core.wxBusiness.game.entity.enums.PassiveSkillType;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemDefaultPassiveSaveReq {
    private String itemId;
    private PassiveSkillType passiveType;
    private List<ItemDefaultPassive> passives = new ArrayList<>();
}
