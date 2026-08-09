package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_stage_level")
@BizIdPrefix("PSL")
public class PlayerStageLevel extends WxBaseEntity<PlayerStageLevel> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;
    private String levelId;
    private Boolean cleared;
    private Boolean firstRewardClaimed;
    /** 当日次数归属日期；与服务器当天日期不同则视为 0 次 */
    private LocalDate attemptDate;
    private Integer attemptCount;
}
