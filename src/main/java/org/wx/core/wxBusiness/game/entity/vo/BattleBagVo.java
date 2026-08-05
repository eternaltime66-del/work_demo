package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.BattleBag;

import java.util.ArrayList;
import java.util.List;

@Data
public class BattleBagVo {

    private List<BattleBag> items = new ArrayList<>();
    private List<EquipSlotVo> equipSlots = new ArrayList<>();
}
