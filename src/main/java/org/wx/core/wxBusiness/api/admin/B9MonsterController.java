package org.wx.core.wxBusiness.api.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.service.MonsterService;
import org.wx.core.wxBusiness.game.service.StageLevelMonsterService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-怪物配置
 */
@RestController
@RequestMapping("/back/monster")
public class B9MonsterController {

    @Resource
    public MonsterService monsterService;
    @Resource
    public StageLevelMonsterService stageLevelMonsterService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<Monster>> list(@RequestBody Monster entity) {
        entity.clearEmptyString();
        IPage<Monster> page = monsterService.pageQuery(entity);
        return WxResult.page(page);
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody Monster entity) {
        entity.clearEmptyString();
        monsterService.savePrepared(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody Monster entity) {
        stageLevelMonsterService.removeByMonsterId(entity.getId());
        monsterService.removeById(entity.getId());
        return WxResult.success();
    }
}
