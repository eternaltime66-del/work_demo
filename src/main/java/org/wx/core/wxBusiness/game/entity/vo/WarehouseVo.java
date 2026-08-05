package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.WarehouseItem;

import java.util.ArrayList;
import java.util.List;

@Data
public class WarehouseVo {

    private String warehouseId;
    private Integer maxSlots;
    private Integer usedSlots;
    private List<WarehouseItem> slots = new ArrayList<>();
}
