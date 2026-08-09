package org.wx.core.wxBusiness.game.entity.enums;

/**
 * 关卡树节点类型：分类 → 大关 → 小关
 */
public enum StageKind {
    /** 分类（如主线） */
    TYPE,
    /** 大关 / 章 */
    CHAPTER,
    /** 小关（可摆怪、可开战） */
    LEVEL
}
