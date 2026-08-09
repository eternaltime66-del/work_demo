package org.wx.core.wxBusiness.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wx.core.wxBase.annotation.BizIdPrefix;
import org.wx.core.wxBase.base.WxBaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_player_stage_chapter")
@BizIdPrefix("PSC")
public class PlayerStageChapter extends WxBaseEntity<PlayerStageChapter> {

    @TableId(type = IdType.INPUT)
    private String id;

    private String uid;
    private String chapterId;
    private Boolean cleared;
    private Boolean firstRewardClaimed;
}
