package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 玩家角色分类。
 */
public enum PlayerRoleCategory {
    HERO("主角"),
    PARTNER("伙伴"),
    SUMMON("召唤物");

    private final String label;

    PlayerRoleCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
