package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 怪物角色分类。
 */
public enum MonsterCategory {
    MONSTER("怪物"),
    SUMMON("召唤物");

    private final String label;

    MonsterCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
