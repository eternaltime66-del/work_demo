package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.mapper.StageLevelMonsterMapper;
import org.wx.core.wxBusiness.game.unit.BattleGrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StageLevelMonsterService extends WxServiceImpl<StageLevelMonsterMapper, StageLevelMonster> {

    @Resource
    private MonsterService monsterService;
    @Resource
    private StageService stageService;
    @Resource
    private ActiveSkillService activeSkillService;

    public List<StageLevelMonster> listByLevelId(String levelId) {
        List<StageLevelMonster> list = this.find()
                .eq(StageLevelMonster::getLevelId, levelId)
                .orderByAsc(StageLevelMonster::getSort)
                .list();
        fillMonster(list);
        return list;
    }

    public void fillMonster(List<StageLevelMonster> list) {
        if (list == null) {
            return;
        }
        for (StageLevelMonster item : list) {
            Monster m = monsterService.getById(item.getMonsterId());
            if (m == null) {
                continue;
            }
            m.applyRaritySize();
            item.setMonsterName(m.getName());
            item.setRarity(m.getRarity());
            item.setGridH(m.getGridH());
            item.setGridW(m.getGridW());
            item.setBaseHp(m.getBaseHp());
            item.setBaseAtk(m.getBaseAtk());
            item.setBaseDef(m.getBaseDef());
            item.setBaseAction(m.getBaseAction());
            item.setRemarkText(m.getRemark());
            item.setNormalSkillId(m.getNormalSkillId());
            item.setSmallSkillId(m.getSmallSkillId());
            item.setUltimateSkillId(m.getUltimateSkillId());
            item.setNormalSkillName(skillName(m.getNormalSkillId()));
            item.setSmallSkillName(skillName(m.getSmallSkillId()));
            item.setUltimateSkillName(skillName(m.getUltimateSkillId()));
        }
    }

    private String skillName(String skillId) {
        if (Wx.isEmpty(skillId)) {
            return null;
        }
        ActiveSkill sk = activeSkillService.getById(skillId);
        return sk == null ? null : sk.getName();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOne(StageLevelMonster entity) {
        ErrorFactory.throwError(entity == null, "配置不能为空");
        ErrorFactory.throwError(Wx.isEmpty(entity.getLevelId()), "小关不能为空");
        ErrorFactory.throwError(Wx.isEmpty(entity.getMonsterId()), "怪物不能为空");
        stageService.requireLevel(entity.getLevelId());
        Monster monster = resolveMonster(entity.getMonsterId());

        int posCol = BattleGrid.nvl(entity.getPosCol(), 0);
        int posRow = BattleGrid.nvl(entity.getPosRow(), 0);
        entity.setPosCol(posCol);
        entity.setPosRow(posRow);
        BattleGrid.assertPlace(posCol, posRow, monster.getGridH(), monster.getGridW(), "怪物");
        assertNoOverlap(entity.getLevelId(), entity.getId(), posCol, posRow, monster.getGridH(), monster.getGridW());

        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        this.saveOrUpdate(entity);
    }

    /**
     * 添加怪物并随机找一个不重叠、可放下的位置
     */
    @Transactional(rollbackFor = Exception.class)
    public StageLevelMonster addRandom(String levelId, String monsterId) {
        ErrorFactory.throwError(Wx.isEmpty(levelId), "小关不能为空");
        ErrorFactory.throwError(Wx.isEmpty(monsterId), "怪物不能为空");
        stageService.requireLevel(levelId);
        Monster monster = resolveMonster(monsterId);

        List<int[]> slots = new ArrayList<>();
        for (int r = 0; r < BattleGrid.ROWS; r++) {
            for (int c = 0; c < BattleGrid.COLS; c++) {
                if (c + monster.getGridW() > BattleGrid.COLS || r + monster.getGridH() > BattleGrid.ROWS) {
                    continue;
                }
                slots.add(new int[]{c, r});
            }
        }
        Collections.shuffle(slots);
        boolean[][] occupied = buildOccupied(levelId, null);
        for (int[] slot : slots) {
            boolean[][] trial = copyBoard(occupied);
            if (!BattleGrid.tryMark(trial, slot[0], slot[1], monster.getGridH(), monster.getGridW())) {
                continue;
            }
            StageLevelMonster row = new StageLevelMonster();
            row.setLevelId(levelId);
            row.setMonsterId(monsterId);
            row.setPosCol(slot[0]);
            row.setPosRow(slot[1]);
            row.setSort(0);
            this.save(row);
            fillMonster(List.of(row));
            return row;
        }
        ErrorFactory.throwError(true, "没有可放置的空位");
        return null;
    }

    public void assertNoOverlap(String levelId, String excludeId, int posCol, int posRow, int gridH, int gridW) {
        boolean[][] board = buildOccupied(levelId, excludeId);
        BattleGrid.mark(board, posCol, posRow, gridH, gridW);
    }

    private boolean[][] buildOccupied(String levelId, String excludeId) {
        List<StageLevelMonster> others = this.find().eq(StageLevelMonster::getLevelId, levelId).list();
        boolean[][] board = BattleGrid.emptyBoard();
        for (StageLevelMonster other : others) {
            if (excludeId != null && excludeId.equals(other.getId())) {
                continue;
            }
            Monster om = resolveMonster(other.getMonsterId());
            BattleGrid.mark(board,
                    BattleGrid.nvl(other.getPosCol(), 0),
                    BattleGrid.nvl(other.getPosRow(), 0),
                    om.getGridH(), om.getGridW());
        }
        return board;
    }

    private static boolean[][] copyBoard(boolean[][] src) {
        boolean[][] copy = BattleGrid.emptyBoard();
        for (int r = 0; r < BattleGrid.ROWS; r++) {
            System.arraycopy(src[r], 0, copy[r], 0, BattleGrid.COLS);
        }
        return copy;
    }

    private Monster resolveMonster(String monsterId) {
        Monster monster = monsterService.getById(monsterId);
        ErrorFactory.throwError(monster == null, "怪物不存在");
        monster.applyRaritySize();
        return monster;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByLevelId(String levelId) {
        this.remove(this.find().eq(StageLevelMonster::getLevelId, levelId).wrapper());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByMonsterId(String monsterId) {
        this.remove(this.find().eq(StageLevelMonster::getMonsterId, monsterId).wrapper());
    }
}
