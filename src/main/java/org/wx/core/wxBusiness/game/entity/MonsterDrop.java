package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

import java.math.BigDecimal;

/**
 * 怪物掉落配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_monster_drop")
@BizIdPrefix("MDP")
public class MonsterDrop extends WxBaseEntity<MonsterDrop> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 怪物 id */
    private String monsterId;

    /** 物品 id */
    private String itemId;

    /** 掉落概率（单位 1%，100=100%；各配置独立判定） */
    private BigDecimal dropRate;

    /** 最小数量（与 maxQty 共同参与非线性随机） */
    private Integer minQty;

    /** 最大数量 */
    private Integer maxQty;

    private Integer sort;

    /** 是否启用 */
    private Boolean enable;

    private String remark;

    @TableField(exist = false)
    private String itemName;
}
