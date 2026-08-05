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
        ensureAllRolesNormalSkill(uid);
    }

    /**
     * 登录后检查：没有主角则发放一个
     */
    @Transactional(rollbackFor = Exception.class)
    public void ensureMainRole(String uid) {
        if (Wx.isEmpty(uid)) {
            return;
        }
        long hasMain = this.find()
                .eq(PlayerRole::getUid, uid)
                .eq(PlayerRole::getMainRole, true)
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
        role.setLifeStealRatio(nvlRatio(conf.getLifeStealRatio(), BigDecimal.ZERO));
        role.setAtkSpeedUpRatio(nvlRatio(conf.getAtkSpeedUpRatio(), BigDecimal.ZERO));
        role.setAtkSpeedDownRatio(nvlRatio(conf.getAtkSpeedDownRatio(), BigDecimal.ZERO));
        role.setMainRole(Boolean.TRUE.equals(conf.getMainRole()));
        if (Boolean.TRUE.equals(role.getMainRole())) {
            role.setGridH(2);
            role.setGridW(2);
        } else {
            role.setGridH(1);
            role.setGridW(1);
        }
        this.save(role);
        playerRoleSkillService.ensureNormalSkill(role.getId());
        return role;
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
        if (role.getLifeStealRatio() == null) {
            role.setLifeStealRatio(BigDecimal.ZERO);
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

    /** 给该玩家全部角色补发默认普攻 */
    @Transactional(rollbackFor = Exception.class)
    public void ensureAllRolesNormalSkill(String uid) {
        if (Wx.isEmpty(uid)) {
            return;
        }
        for (PlayerRole role : listByUid(uid)) {
            playerRoleSkillService.ensureNormalSkill(role.getId());
        }
    }
}
