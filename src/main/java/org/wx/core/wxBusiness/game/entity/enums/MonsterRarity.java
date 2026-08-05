package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 怪物稀有度（高*宽）；特殊占地可自定义
 */
public enum MonsterRarity {
    /** 普通 1*1 */
    NORMAL(1, 1, true),
    /** 稀有 1*2 */
    RARE(1, 2, true),
    /** 史诗 2*2 */
    EPIC(2, 2, true),
    /** BOSS 2*4 */
    BOSS(2, 4, true),
    /** 特殊：占地可编辑 */
    SPECIAL(1, 1, false);

    /** 固定稀有度默认高；特殊仅作新建默认值 */
    public final int gridH;
    /** 固定稀有度默认宽；特殊仅作新建默认值 */
    public final int gridW;
    /** true=按稀有度强制占地，false=可编辑 */
    public final boolean fixedSize;

    MonsterRarity(int gridH, int gridW, boolean fixedSize) {
        this.gridH = gridH;
        this.gridW = gridW;
        this.fixedSize = fixedSize;
    }
}
