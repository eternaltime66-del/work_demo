package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.battle.AtkSpeedCalcUnit;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.entity.RoleBaseStat;
import org.wx.core.wxBusiness.game.entity.enums.PlayerRoleCategory;
import org.wx.core.wxBusiness.game.mapper.PlayerRoleMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlayerRoleService extends WxServiceImpl<PlayerRoleMapper, PlayerRole> {

    @Resource
    private RoleBaseStatService roleBaseStatService;
    @Lazy
    @Resource
    private PlayerRoleSkillService playerRoleSkillService;

    public List<PlayerRole> listByUid(String uid) {
        return this.find().eq(PlayerRole::getUid, uid).list();
    }

    /**
     * 给玩家发放所有「默认拥有」配置角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantDefaultRoles(String uid) {
        List<RoleBaseStat> defaults = roleBaseStatService.find()
                .eq(RoleBaseStat::getDefaultOwn, true)
                .list();
        for (RoleBaseStat conf : defaults) {
            long exists = this.find()
                    .eq(PlayerRole::getUid, uid)
                    .eq(PlayerRole::getBaseStatId, conf.getId())
                    .count();
            if (exists == 0) {
                createFromBaseStat(uid, conf.getId());
            }
        }
        ensureMainRole(uid);
    }

    /**
     * 登录后检查：没有主角则发放一个
     */
    @Transactional(rollbackFor = Exception.class)
    public void ensureMainRole(String uid) {
        if (Wx.isEmpty(uid)) {
            return;
        }
        long hasMain = this.lambdaQuery()
                .eq(PlayerRole::getUid, uid)
                .and(w -> w.eq(PlayerRole::getMainRole, true)
                        .or()
                        .eq(PlayerRole::getRoleCategory, PlayerRoleCategory.HERO))
                .count();
        if (hasMain > 0) {
            return;
        }
        RoleBaseStat mainConf = roleBaseStatService.find()
                .eq(RoleBaseStat::getMainRole, true)
                .one();
        ErrorFactory.throwError(mainConf == null, "系统未配置主角角色");
        createFromBaseStat(uid, mainConf.getId());
    }

    /**
     * 按基础数值配置给玩家创建持有角色（拷贝数值）
     */
    @Transactional(rollbackFor = Exception.class)
    public PlayerRole createFromBaseStat(String uid, String baseStatId) {
        return createFromBaseStat(uid, baseStatId, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerRole createFromBaseStat(String uid, String baseStatId, PlayerRoleCategory preferCategory) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        ErrorFactory.throwError(Wx.isEmpty(baseStatId), "基础数值配置不能为空");

        RoleBaseStat conf = roleBaseStatService.getById(baseStatId);
        ErrorFactory.throwError(conf == null, "基础数值配置不存在");

        PlayerRole role = new PlayerRole();
        role.setUid(uid);
        role.setBaseStatId(conf.getId());
        role.setName(conf.getName());
        role.setBaseAtk(nvl(conf.getBaseAtk()));
        role.setBaseHp(nvl(conf.getBaseHp()));
        role.setBaseDef(nvl(conf.getBaseDef()));
        role.setBaseAtkSpeed(conf.getBaseAtkSpeed() != null && conf.getBaseAtkSpeed().compareTo(BigDecimal.ZERO) > 0
                ? AtkSpeedCalcUnit.normalizeAtkSpeedBd(conf.getBaseAtkSpeed())
                : AtkSpeedCalcUnit.atkSpeedFromAction(conf.getBaseAction()));
        role.setBaseAction(AtkSpeedCalcUnit.actionFromAtkSpeed(role.getBaseAtkSpeed()));
        role.setExtraHp(nvl(conf.getExtraHp()));
        role.setExtraAtk(nvl(conf.getExtraAtk()));
        role.setExtraDef(nvl(conf.getExtraDef()));
        role.setDealDmgRatio(nvlRatio(conf.getDealDmgRatio(), HUNDRED));
        role.setTakenDmgRatio(nvlRatio(conf.getTakenDmgRatio(), HUNDRED));
        role.setDealElementDmgRatio(nvlRatio(conf.getDealElementDmgRatio(), HUNDRED));
        role.setTakenElementDmgRatio(nvlRatio(conf.getTakenElementDmgRatio(), HUNDRED));
        role.setDealPhysDmgRatio(nvlRatio(conf.getDealPhysDmgRatio(), HUNDRED));
        role.setTakenPhysDmgRatio(nvlRatio(conf.getTakenPhysDmgRatio(), HUNDRED));
        role.setLifeStealRatio(nvlRatio(conf.getLifeStealRatio(), BigDecimal.ZERO));
        role.setFinalAtkRatio(nvlRatio(conf.getFinalAtkRatio(), HUNDRED));
        role.setFinalHpRatio(nvlRatio(conf.getFinalHpRatio(), HUNDRED));
        role.setFinalDefRatio(nvlRatio(conf.getFinalDefRatio(), HUNDRED));
        role.setAtkSpeedUpRatio(nvlRatio(conf.getAtkSpeedUpRatio(), BigDecimal.ZERO));
        role.setAtkSpeedDownRatio(nvlRatio(conf.getAtkSpeedDownRatio(), BigDecimal.ZERO));
        role.setInheritAtkRatio(conf.getInheritAtkRatio());
        role.setInheritDefRatio(conf.getInheritDefRatio());
        role.setInheritHpRatio(conf.getInheritHpRatio());
        PlayerRoleCategory templateCat = conf.getRoleCategory();
        boolean asHero = Boolean.TRUE.equals(conf.getMainRole())
                || templateCat == PlayerRoleCategory.HERO
                || preferCategory == PlayerRoleCategory.HERO;
        if (asHero) {
            assertNoOtherHero(uid, null);
            role.setMainRole(true);
            role.setRoleCategory(PlayerRoleCategory.HERO);
            role.setGridH(3);
            role.setGridW(2);
            role.setInheritAtkRatio(null);
            role.setInheritDefRatio(null);
            role.setInheritHpRatio(null);
        } else {
            role.setMainRole(false);
            PlayerRoleCategory cat;
            if (preferCategory == PlayerRoleCategory.SUMMON || preferCategory == PlayerRoleCategory.PARTNER) {
                cat = preferCategory;
            } else if (templateCat == PlayerRoleCategory.SUMMON || templateCat == PlayerRoleCategory.PARTNER) {
                cat = templateCat;
            } else {
                cat = PlayerRoleCategory.PARTNER;
            }
            role.setRoleCategory(cat);
            role.setGridH(1);
            role.setGridW(1);
            if (cat == PlayerRoleCategory.SUMMON) {
                role.setInheritAtkRatio(nvlRatio(conf.getInheritAtkRatio(), HUNDRED));
                role.setInheritDefRatio(nvlRatio(conf.getInheritDefRatio(), HUNDRED));
                role.setInheritHpRatio(nvlRatio(conf.getInheritHpRatio(), HUNDRED));
                role.setBaseAtk(0);
                role.setBaseHp(0);
                role.setBaseDef(0);
            }
        }
        this.save(role);
        return role;
    }

    /** 后台更新：主角唯一、分类锁定、不可改为非主角 */
    public void updateAdmin(PlayerRole entity) {
        ErrorFactory.throwError(entity == null || Wx.isEmpty(entity.getId()), "角色不存在");
        PlayerRole db = this.getById(entity.getId());
        ErrorFactory.throwError(db == null, "角色不存在");

        PlayerRoleCategory oldCat = resolveCategory(db);
        PlayerRoleCategory newCat = entity.getRoleCategory() != null ? entity.getRoleCategory() : oldCat;

        if (oldCat == PlayerRoleCategory.HERO) {
            ErrorFactory.throwError(newCat != PlayerRoleCategory.HERO, "主角的角色分类不可更改");
            entity.setRoleCategory(PlayerRoleCategory.HERO);
            entity.setMainRole(true);
        } else {
            ErrorFactory.throwError(newCat == PlayerRoleCategory.HERO, "不能将角色改为主角，请通过主角模板发放");
            entity.setRoleCategory(newCat == null ? PlayerRoleCategory.PARTNER : newCat);
            entity.setMainRole(false);
        }
        if (entity.getRoleCategory() == PlayerRoleCategory.HERO) {
            assertNoOtherHero(db.getUid(), db.getId());
            entity.setGridH(3);
            entity.setGridW(2);
        }
        prepareRatios(entity);
        this.updateById(entity);
    }

    /** 后台删除：主角不可删 */
    public void removeAdmin(String id) {
        ErrorFactory.throwError(Wx.isEmpty(id), "角色不存在");
        PlayerRole db = this.getById(id);
        ErrorFactory.throwError(db == null, "角色不存在");
        ErrorFactory.throwError(resolveCategory(db) == PlayerRoleCategory.HERO, "主角不可删除");
        this.removeById(id);
    }

    private void assertNoOtherHero(String uid, String excludeId) {
        var q = this.lambdaQuery()
                .eq(PlayerRole::getUid, uid)
                .and(w -> w.eq(PlayerRole::getMainRole, true)
                        .or()
                        .eq(PlayerRole::getRoleCategory, PlayerRoleCategory.HERO));
        if (!Wx.isEmpty(excludeId)) {
            q.ne(PlayerRole::getId, excludeId);
        }
        ErrorFactory.throwError(q.count() > 0, "每个玩家只能有一个主角");
    }

    private static PlayerRoleCategory resolveCategory(PlayerRole role) {
        if (role == null) {
            return PlayerRoleCategory.PARTNER;
        }
        if (role.getRoleCategory() != null) {
            return role.getRoleCategory();
        }
        return Boolean.TRUE.equals(role.getMainRole()) ? PlayerRoleCategory.HERO : PlayerRoleCategory.PARTNER;
    }

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private Integer nvl(Integer v) {
        return v == null ? 0 : v;
    }

    /** 比例单位为 1%；空值用默认（造成/受到伤害默认 100） */
    private BigDecimal nvlRatio(BigDecimal v, BigDecimal def) {
        return v == null ? def : v;
    }

    public void prepareRatios(PlayerRole role) {
        if (role == null) {
            return;
        }
        if (role.getDealDmgRatio() == null) {
            role.setDealDmgRatio(HUNDRED);
        }
        if (role.getTakenDmgRatio() == null) {
            role.setTakenDmgRatio(HUNDRED);
        }
        if (role.getDealElementDmgRatio() == null) {
            role.setDealElementDmgRatio(HUNDRED);
        }
        if (role.getTakenElementDmgRatio() == null) {
            role.setTakenElementDmgRatio(HUNDRED);
        }
        if (role.getDealPhysDmgRatio() == null) {
            role.setDealPhysDmgRatio(HUNDRED);
        }
        if (role.getTakenPhysDmgRatio() == null) {
            role.setTakenPhysDmgRatio(HUNDRED);
        }
        if (role.getLifeStealRatio() == null) {
            role.setLifeStealRatio(BigDecimal.ZERO);
        }
        if (role.getFinalAtkRatio() == null) {
            role.setFinalAtkRatio(HUNDRED);
        }
        if (role.getFinalHpRatio() == null) {
            role.setFinalHpRatio(HUNDRED);
        }
        if (role.getFinalDefRatio() == null) {
            role.setFinalDefRatio(HUNDRED);
        }
        if (role.getAtkSpeedUpRatio() == null) {
            role.setAtkSpeedUpRatio(BigDecimal.ZERO);
        }
        if (role.getAtkSpeedDownRatio() == null) {
            role.setAtkSpeedDownRatio(BigDecimal.ZERO);
        }
        syncActionFromAtkSpeed(role);
    }

    /** 以攻速为准反推行动值；缺攻速时用行动值反推攻速，默认攻速 1 */
    public void syncActionFromAtkSpeed(PlayerRole role) {
        if (role == null) {
            return;
        }
        if (role.getBaseAtkSpeed() != null && role.getBaseAtkSpeed().compareTo(BigDecimal.ZERO) > 0) {
            role.setBaseAtkSpeed(AtkSpeedCalcUnit.normalizeAtkSpeedBd(role.getBaseAtkSpeed()));
            role.setBaseAction(AtkSpeedCalcUnit.actionFromAtkSpeed(role.getBaseAtkSpeed()));
            return;
        }
        if (role.getBaseAction() != null && role.getBaseAction() > 0) {
            role.setBaseAtkSpeed(AtkSpeedCalcUnit.atkSpeedFromAction(role.getBaseAction()));
            return;
        }
        role.setBaseAtkSpeed(BigDecimal.ONE);
        role.setBaseAction(AtkSpeedCalcUnit.actionFromAtkSpeed(BigDecimal.ONE));
    }

}
