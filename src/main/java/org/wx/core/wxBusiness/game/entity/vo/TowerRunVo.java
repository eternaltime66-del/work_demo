package org.wx.core.wxBusiness.game.entity.vo;

import lombok.Data;
import org.wx.core.wxBusiness.game.entity.enums.TowerRunStatus;

@Data
public class TowerRunVo {
    private TowerRunStatus status;
    private String currentLevelId;
    private String currentLevelName;
    private String currentLevelCode;
    private String chapterName;
    /** 无进行中时：下一局将从该关开始（通常为第一关） */
    private String startLevelId;
    private boolean hasRunning;
}
