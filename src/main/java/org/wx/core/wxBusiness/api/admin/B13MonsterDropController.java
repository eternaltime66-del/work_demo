package org.wx.core.wxBusiness.api.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.game.entity.MonsterDrop;
import org.wx.core.wxBusiness.game.service.MonsterDropService;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.util.List;

/**
 * 后台-怪物掉落
 */
@RestController
@RequestMapping("/back/monster/drop")
public class B13MonsterDropController {

    @Resource
    public MonsterDropService monsterDropService;

    @PostMapping("/list")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<MonsterDrop>> list(@RequestBody MonsterDrop entity) {
        entity.clearEmptyString();
        IPage<MonsterDrop> page = monsterDropService.pageQuery(entity);
        monsterDropService.fillItemName(page.getRecords());
        return WxResult.page(page);
    }

    @PostMapping("/listByMonster")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<List<MonsterDrop>> listByMonster(@ParamCheck String monsterId) {
        return WxResult.success(monsterDropService.listByMonsterId(monsterId));
    }

    @PostMapping("/update")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> update(@RequestBody MonsterDrop entity) {
        entity.clearEmptyString();
        monsterDropService.savePrepared(entity);
        return WxResult.success();
    }

    @PostMapping("/remove")
    @WxRequestLog()
    @NeedHeader(roles = {MemberRole.ADMIN})
    public WxResult<?> remove(@RequestBody MonsterDrop entity) {
        monsterDropService.removeById(entity.getId());
        return WxResult.success();
    }
}
