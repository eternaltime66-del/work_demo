package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * 物品-武器扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_item_weapon")
@BizIdPrefix("WPN")
public class ItemWeapon extends WxBaseEntity<ItemWeapon> {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 物品主表 id */
    private String itemId;

    /** 基础攻击力 */
    private Integer baseAtk;

    /** 增加攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedUpRatio;

    /** 减少攻速（单位 1%，叠乘） */
    private BigDecimal atkSpeedDownRatio;

    /**
     * 普攻技能 id（可选）。
     * 有配置时：装备该武器的玩家，战斗中角色普攻替换为此技能；
     * 未配置 / 清空时：使用通用默认普攻。
     * ALWAYS：允许把字段更新为 null（清空绑定）。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String normalSkillId;

    private String remark;
}
