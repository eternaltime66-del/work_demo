package org.wx.core.wxBusiness.game.battle;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.enums.AttrModifyKey;

/**
 * 战斗中可撤销的属性 buff（时长到期 / 持续效果条件撤销）。
 */
@Data
public class TimedAttrBuff {

    /** 同源键：刷新时长不叠层 */
    private String sourceKey;

    private AttrModifyKey attrKey;

    /** 已应用到面板的平坦变化（ATK/DEF/MAX_HP / FINAL_* 折算后） */
    private int appliedFlat;

    /** ADD_PERCENT 键写入修饰袋的百分点（可正负） */
    private int ratioAdd;

    /** MULT_PERCENT 键已乘到修饰袋的因子（如 1.1）；撤销时除回 */
    private double ratioFactor = 1D;

    /** 到期全局行动值；null 表示由持续效果条件维持，不按时间轴到期 */
    private Integer expireAtElapsed;

    /** 持续效果绑定 */
    private boolean sustained;

    private String label;
}
