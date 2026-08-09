package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PlayerLayout;
import org.wx.core.wxBusiness.game.entity.PlayerRole;
import org.wx.core.wxBusiness.game.mapper.PlayerLayoutMapper;
import org.wx.core.wxBusiness.game.unit.BattleGrid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PlayerLayoutService extends WxServiceImpl<PlayerLayoutMapper, PlayerLayout> {

    @Resource
    private PlayerRoleService playerRoleService;

    public List<PlayerLayout> listByUid(String uid) {
        List<PlayerLayout> list = this.find().eq(PlayerLayout::getUid, uid).list();
        fillRole(list);
        return list;
    }

    public void fillRole(List<PlayerLayout> list) {
        if (list == null) {
            return;
        }
        for (PlayerLayout item : list) {
            PlayerRole role = playerRoleService.getById(item.getRoleId());
            if (role == null) {
                continue;
            }
            item.setRoleName(role.getName());
            item.setMainRole(role.getMainRole());
            item.setGridH(role.getGridH());
            item.setGridW(role.getGridW());
            item.setBaseHp(role.getBaseHp());
            item.setExtraHp(role.getExtraHp());
        }
    }

    /**
     * 全量覆盖保存布阵
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PlayerLayout> replaceAll(String uid, List<PlayerLayout> items) {
        ErrorFactory.throwError(Wx.isEmpty(uid), "uid不能为空");
        if (items == null) {
            items = new ArrayList<>();
        }

        boolean[][] board = BattleGrid.emptyBoard();
        Set<String> roleIds = new HashSet<>();
        List<PlayerLayout> toSave = new ArrayList<>();

        for (PlayerLayout item : items) {
            ErrorFactory.throwError(item == null || Wx.isEmpty(item.getRoleId()), "角色不能为空");
            ErrorFactory.throwError(!roleIds.add(item.getRoleId()), "同一角色不能重复上阵");

            PlayerRole role = playerRoleService.getById(item.getRoleId());
            ErrorFactory.throwError(role == null, "角色不存在");
            ErrorFactory.throwError(!uid.equals(role.getUid()), "无权使用该角色");

            int gridH = BattleGrid.nvl(role.getGridH(), Boolean.TRUE.equals(role.getMainRole()) ? 3 : 1);
            int gridW = BattleGrid.nvl(role.getGridW(), Boolean.TRUE.equals(role.getMainRole()) ? 2 : 1);
            int posCol = BattleGrid.nvl(item.getPosCol(), 0);
            int posRow = BattleGrid.nvl(item.getPosRow(), 0);
            BattleGrid.assertPlace(posCol, posRow, gridH, gridW, role.getName() == null ? "角色" : role.getName());
            BattleGrid.mark(board, posCol, posRow, gridH, gridW);

            PlayerLayout row = new PlayerLayout();
            row.setUid(uid);
            row.setRoleId(role.getId());
            row.setPosCol(posCol);
            row.setPosRow(posRow);
            toSave.add(row);
        }

        this.remove(this.find().eq(PlayerLayout::getUid, uid).wrapper());
        for (PlayerLayout row : toSave) {
            this.save(row);
        }
        return listByUid(uid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleId(String roleId) {
        if (Wx.isEmpty(roleId)) {
            return;
        }
        this.remove(this.find().eq(PlayerLayout::getRoleId, roleId).wrapper());
    }

    /**
     * 若无布局则默认把主角放在 (1,1) —— 横 2 格、竖 3 格
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PlayerLayout> listOrDefault(String uid) {
        List<PlayerLayout> list = listByUid(uid);
        if (!list.isEmpty()) {
            return list;
        }
        PlayerRole main = playerRoleService.find()
                .eq(PlayerRole::getUid, uid)
                .eq(PlayerRole::getMainRole, true)
                .one();
        if (main == null) {
            return list;
        }
        PlayerLayout def = new PlayerLayout();
        def.setRoleId(main.getId());
        def.setPosCol(1);
        def.setPosRow(1);
        return replaceAll(uid, List.of(def));
    }
}
