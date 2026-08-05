package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.PlayerRole;

@Data
public class PrepSummaryVo {

    /** 主角 */
    private PlayerRole mainRole;

    private BattleBagVo battleBag;

    private WarehouseVo warehouse;
}
