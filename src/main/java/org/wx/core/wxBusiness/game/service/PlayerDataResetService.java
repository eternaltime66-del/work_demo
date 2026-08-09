package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.account.entity.Member;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.account.service.MemberService;
import org.wx.core.wxBusiness.game.entity.BattleBag;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.PlayerLayout;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.PlayerRoleSkill;
import org.wx.core.wxBusiness.game.entity.PlayerStageChapter;
import org.wx.core.wxBusiness.game.entity.PlayerStageLevel;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.entity.PlayerTowerRun;
import org.wx.core.wxBusiness.game.entity.WarehouseItem;

import java.util.List;

/**
 * 后台：重置玩家游戏数据（保留账号登录，清进度/背包/角色后按新人初始化）
 */
@Service
public class PlayerDataResetService {

    @Resource
    private MemberService memberService;
    @Resource
    private PlayerRoleService playerRoleService;
    @Resource
    private PlayerRoleSkillService playerRoleSkillService;
    @Resource
    private PlayerLayoutService playerLayoutService;
    @Resource
    private PlayerEquipService playerEquipService;
    @Resource
    private BattleBagService battleBagService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseItemService warehouseItemService;
    @Resource
    private StageProgressService stageProgressService;
    @Resource
    private PlayerStageChapterService playerStageChapterService;
    @Resource
    private PlayerStaminaService playerStaminaService;
    @Resource
    private TowerRunService towerRunService;

    @Transactional(rollbackFor = Exception.class)
    public void resetPlayerData(String uid) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "玩家不能为空");
        Member member = memberService.getById(uid);
        ErrorFactory.throwError(member == null, "玩家不存在");
        ErrorFactory.throwError(member.getMemberRole() != MemberRole.USER, "只能重置普通玩家数据");

        // 角色技能 → 角色
        List<PlayerRole> roles = playerRoleService.find().eq(PlayerRole::getUid, uid).list();
        for (PlayerRole role : roles) {
            if (role == null || Wx.isEmpty(role.getId())) {
                continue;
            }
            playerRoleSkillService.remove(
                    playerRoleSkillService.find().eq(PlayerRoleSkill::getRoleId, role.getId()).wrapper());
        }
        playerRoleService.remove(playerRoleService.find().eq(PlayerRole::getUid, uid).wrapper());

        playerLayoutService.remove(playerLayoutService.find().eq(PlayerLayout::getUid, uid).wrapper());
        playerEquipService.remove(playerEquipService.find().eq(PlayerEquip::getUid, uid).wrapper());
        battleBagService.remove(battleBagService.find().eq(BattleBag::getUid, uid).wrapper());

        warehouseItemService.remove(warehouseItemService.find().eq(WarehouseItem::getUid, uid).wrapper());

        stageProgressService.remove(stageProgressService.find().eq(PlayerStageLevel::getUid, uid).wrapper());
        playerStageChapterService.remove(
                playerStageChapterService.find().eq(PlayerStageChapter::getUid, uid).wrapper());
        playerStaminaService.remove(playerStaminaService.find().eq(PlayerStamina::getUid, uid).wrapper());
        towerRunService.remove(towerRunService.find().eq(PlayerTowerRun::getUid, uid).wrapper());

        // 按新人初始化（账号 / 钱包保留）
        warehouseService.ensureWarehouse(uid);
        playerRoleService.grantDefaultRoles(uid);
        playerStaminaService.getOrCreate(uid);
    }
}
