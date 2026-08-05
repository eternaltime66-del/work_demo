package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.annotation.RedisLock;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.BattleBag;
import org.wx.core.wxBusiness.game.entity.PlayerEquip;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.WarehouseItem;
import org.wx.core.wxBusiness.game.entity.enums.EquipSlot;
import org.wx.core.wxBusiness.game.battle.AtkSpeedCalcUnit;
import org.wx.core.wxBusiness.game.battle.FinalStatCalcUnit;
import org.wx.core.wxBusiness.game.entity.vo.BattleBagVo;
import org.wx.core.wxBusiness.game.entity.vo.EquipBonusVo;
import org.wx.core.wxBusiness.game.entity.vo.PrepSummaryVo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PrepService {

    @Resource
    private PlayerRoleService playerRoleService;
    @Resource
    private PlayerEquipService playerEquipService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private BattleBagService battleBagService;
    @Resource
    private EquipBonusService equipBonusService;
    @Resource
    private AtkSpeedService atkSpeedService;

    public PrepSummaryVo getSummary(String uid) {
        playerRoleService.ensureMainRole(uid);
        warehouseService.ensureWarehouse(uid);
        PlayerRole main = playerRoleService.find()
                .eq(PlayerRole::getUid, uid)
                .eq(PlayerRole::getMainRole, true)
                .one();
        fillDisplayStats(uid, main);
        PrepSummaryVo vo = new PrepSummaryVo();
        vo.setMainRole(main);
        vo.setBattleBag(buildBattleBagVo(uid));
        vo.setWarehouse(warehouseService.getWarehouseDetail(uid));
        return vo;
    }

    /** 填充含装备加成 / 最终攻速的展示字段 */
    public void fillDisplayStats(String uid, PlayerRole role) {
        if (role == null) {
            return;
        }
        EquipBonusVo bonus = equipBonusService.sumBonus(uid);
        int baseAtk = nvl(role.getBaseAtk()) + nvl(role.getExtraAtk());
        int baseHp = nvl(role.getBaseHp()) + nvl(role.getExtraHp());
        int baseDef = nvl(role.getBaseDef()) + nvl(role.getExtraDef());
        role.setEquipBonusAtk(bonus.getAtk());
        role.setEquipBonusHp(bonus.getHp());
        role.setEquipBonusDef(bonus.getDefense());
        // 总属性 = (基础+额外+装备平坦/OUT基础) × (角色最终比例 + OUT高级最终比例)
        role.setDisplayAtk(Math.max(0, FinalStatCalcUnit.apply(
                baseAtk + bonus.getAtk(), bonus.mergeFinalAtkRatio(role.getFinalAtkRatio()))));
        role.setDisplayHp(FinalStatCalcUnit.applyHp(
                baseHp + bonus.getHp(), bonus.mergeFinalHpRatio(role.getFinalHpRatio())));
        role.setDisplayDef(Math.max(0, FinalStatCalcUnit.apply(
                baseDef + bonus.getDefense(), bonus.mergeFinalDefRatio(role.getFinalDefRatio()))));
        int displayAction = atkSpeedService.calcRoleAction(uid, role, bonus);
        role.setDisplayAction(displayAction);
        role.setDisplayAtkSpeed(AtkSpeedCalcUnit.atkSpeedFromAction(displayAction));
        if (role.getBaseAtkSpeed() == null || role.getBaseAtkSpeed().signum() <= 0) {
            role.setBaseAtkSpeed(AtkSpeedCalcUnit.atkSpeedFromAction(role.getBaseAction()));
        }
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    public BattleBagVo buildBattleBagVo(String uid) {
        PlayerEquip equip = playerEquipService.getOrInit(uid);
        BattleBagVo vo = new BattleBagVo();
        vo.setItems(battleBagService.listByUid(uid));
        vo.setEquipSlots(playerEquipService.buildSlotOverview(equip));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid")
    public void batchWarehouseToBag(String uid, List<Integer> slotNos) {
        ErrorFactory.throwError(slotNos == null || slotNos.isEmpty(), "请选择仓库格子");
        Set<Integer> unique = new LinkedHashSet<>(slotNos);
        for (Integer slotNo : unique) {
            moveWarehouseToBag(uid, slotNo, null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid")
    public void batchBagToWarehouse(String uid, List<String> bagIds) {
        ErrorFactory.throwError(bagIds == null || bagIds.isEmpty(), "请选择背包物品");
        Set<String> unique = new LinkedHashSet<>(bagIds);
        for (String bagId : unique) {
            moveBagToWarehouse(uid, bagId, null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid")
    public void dragTransfer(String uid, String fromType, String fromKey, String toType, String toKey, Integer quantity) {
        ErrorFactory.notEmpty(fromType, "来源类型不能为空");
        ErrorFactory.notEmpty(toType, "目标类型不能为空");
        if ("WAREHOUSE".equals(fromType) && "BAG".equals(toType)) {
            moveWarehouseToBag(uid, Integer.valueOf(fromKey), quantity);
            return;
        }
        if ("BAG".equals(fromType) && "WAREHOUSE".equals(toType)) {
            moveBagToWarehouse(uid, fromKey, quantity);
            return;
        }
        if ("BAG".equals(fromType) && "BAG".equals(toType)) {
            mergeBagItems(uid, fromKey, toKey, quantity);
            return;
        }
        ErrorFactory.throwError(true, "暂不支持该转移方式");
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid")
    public BattleBagVo equipSlot(String uid, String slot, String itemId) {
        EquipSlot equipSlot = EquipSlot.parse(slot);
        ErrorFactory.notNull(equipSlot, "无效的装备槽位");
        playerEquipService.equipSlot(uid, equipSlot, itemId);
        return buildBattleBagVo(uid);
    }

    @Transactional(rollbackFor = Exception.class)
    @RedisLock(key = "uid")
    public BattleBagVo unequipSlot(String uid, String slot) {
        EquipSlot equipSlot = EquipSlot.parse(slot);
        ErrorFactory.notNull(equipSlot, "无效的装备槽位");
        playerEquipService.unequipSlot(uid, equipSlot);
        return buildBattleBagVo(uid);
    }

    private void moveWarehouseToBag(String uid, Integer slotNo, Integer quantity) {
        WarehouseItem row = warehouseService.findSlot(uid, slotNo);
        ErrorFactory.notNull(row, "仓库格子为空");
        int available = row.getQuantity() == null ? 0 : row.getQuantity();
        ErrorFactory.throwError(available <= 0, "仓库格子为空");
        int moveQty = quantity == null || quantity <= 0 ? available : Math.min(quantity, available);
        String itemId = warehouseService.takeFromSlot(uid, slotNo, moveQty);
        battleBagService.addItem(uid, itemId, moveQty);
    }

    private void moveBagToWarehouse(String uid, String bagId, Integer quantity) {
        BattleBag row = battleBagService.getById(bagId);
        ErrorFactory.notNull(row, "背包物品不存在");
        ErrorFactory.notEquals(uid, row.getUid(), "背包物品不匹配");
        int available = row.getQuantity() == null ? 0 : row.getQuantity();
        ErrorFactory.throwError(available <= 0, "背包物品数量不足");
        int moveQty = quantity == null || quantity <= 0 ? available : Math.min(quantity, available);
        String itemId = battleBagService.takeByBagId(uid, bagId, moveQty);
        warehouseService.addItem(uid, itemId, moveQty);
    }

    private void mergeBagItems(String uid, String fromBagId, String toBagId, Integer quantity) {
        if (Wx.isEmpty(toBagId) || fromBagId.equals(toBagId)) {
            return;
        }
        BattleBag from = battleBagService.getById(fromBagId);
        BattleBag to = battleBagService.getById(toBagId);
        ErrorFactory.notNull(from, "来源背包物品不存在");
        ErrorFactory.notNull(to, "目标背包物品不存在");
        ErrorFactory.notEquals(uid, from.getUid(), "来源不匹配");
        ErrorFactory.notEquals(uid, to.getUid(), "目标不匹配");
        ErrorFactory.notEquals(from.getItemId(), to.getItemId(), "只能合并相同物品");
        int available = from.getQuantity() == null ? 0 : from.getQuantity();
        int moveQty = quantity == null || quantity <= 0 ? available : Math.min(quantity, available);
        ErrorFactory.throwError(moveQty <= 0, "数量无效");
        battleBagService.takeByBagId(uid, fromBagId, moveQty);
        battleBagService.addItem(uid, to.getItemId(), moveQty);
    }
}
