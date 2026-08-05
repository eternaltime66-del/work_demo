package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.enums.MonsterRarity;
import org.wx.core.wxBusiness.game.unit.BattleGrid;

/**
 * 怪物配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_monster")
@BizIdPrefix("MST")
public class Monster extends WxBaseEntity<Monster> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    /** 稀有度：普通/稀有/史诗/BOSS/特殊 */
    private MonsterRarity rarity;

    /** 占地高度 */
    private Integer gridH;

    /** 占地宽度 */
    private Integer gridW;

    private Integer baseAtk;
    private Integer baseHp;
    private Integer baseDef;
    private Integer baseAction;
    private Integer sort;
    private String remark;

    /**
     * 固定稀有度强制默认占地；特殊保留可编辑占地（默认 1*1）
     */
    public void applyRaritySize() {
        MonsterRarity r = this.rarity == null ? MonsterRarity.NORMAL : this.rarity;
        this.rarity = r;
        if (r.fixedSize) {
            this.gridH = r.gridH;
            this.gridW = r.gridW;
            return;
        }
        if (this.gridH == null || this.gridH < 1) {
            this.gridH = r.gridH;
        }
        if (this.gridW == null || this.gridW < 1) {
            this.gridW = r.gridW;
        }
        ErrorFactory.throwError(this.gridH > BattleGrid.ROWS || this.gridW > BattleGrid.COLS,
                "特殊怪物占地不能超过战场（高" + BattleGrid.ROWS + " 宽" + BattleGrid.COLS + "）");
    }
}
