package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;
import org.wx.core.wxBusiness.game.entity.enums.TowerRunStatus;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_tower_run")
@BizIdPrefix("PTR")
public class PlayerTowerRun extends WxBaseEntity<PlayerTowerRun> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;
    private TowerRunStatus status;
    private String currentLevelId;
    /** roleId -> remainingHp JSON */
    private String allyHpJson;
}
