package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.PlayerRole;

import java.util.ArrayList;
import java.util.List;

@Data
public class PrepSummaryVo {

    /** 主角 */
    private PlayerRole mainRole;

    private BattleBagVo battleBag;

    private WarehouseVo warehouse;

    /** 人偶两侧技能槽 1~8（暂留空；技能见角色详情） */
    private List<PrepSkillSlotVo> skillSlots = new ArrayList<>();
}
