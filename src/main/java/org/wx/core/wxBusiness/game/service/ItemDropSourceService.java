package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBusiness.game.entity.Monster;
import org.wx.core.wxBusiness.game.entity.MonsterDrop;
import org.wx.core.wxBusiness.game.entity.StageChapter;
import org.wx.core.wxBusiness.game.entity.StageLevel;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.vo.ItemDropSourceVo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物品掉落来源：怪物掉落 → 关卡配置
 */
@Service
public class ItemDropSourceService {

    @Resource
    private MonsterDropService monsterDropService;
    @Resource
    private StageLevelMonsterService stageLevelMonsterService;
    @Resource
    private StageLevelService stageLevelService;
    @Resource
    private StageChapterService stageChapterService;
    @Resource
    private MonsterService monsterService;

    public boolean hasDropSource(String itemId) {
        if (Wx.isEmpty(itemId)) {
            return false;
        }
        Long cnt = monsterDropService.find()
                .eq(MonsterDrop::getItemId, itemId)
                .eq(MonsterDrop::getEnable, true)
                .count();
        return cnt != null && cnt > 0;
    }

    public List<ItemDropSourceVo> listByItemId(String itemId) {
        if (Wx.isEmpty(itemId)) {
            return List.of();
        }
        List<MonsterDrop> drops = monsterDropService.find()
                .eq(MonsterDrop::getItemId, itemId)
                .eq(MonsterDrop::getEnable, true)
                .orderByAsc(MonsterDrop::getSort)
                .list();
        if (drops.isEmpty()) {
            return List.of();
        }
        Map<String, MonsterDrop> dropByMonster = new LinkedHashMap<>();
        for (MonsterDrop drop : drops) {
            if (drop != null && !Wx.isEmpty(drop.getMonsterId())) {
                dropByMonster.putIfAbsent(drop.getMonsterId(), drop);
            }
        }
        if (dropByMonster.isEmpty()) {
            return List.of();
        }
        List<StageLevelMonster> placements = stageLevelMonsterService.find()
                .in(StageLevelMonster::getMonsterId, dropByMonster.keySet())
                .list();
        if (placements.isEmpty()) {
            return List.of();
        }

        Set<String> levelIds = new LinkedHashSet<>();
        Set<String> monsterIds = new LinkedHashSet<>();
        for (StageLevelMonster p : placements) {
            if (p.getLevelId() != null) {
                levelIds.add(p.getLevelId());
            }
            if (p.getMonsterId() != null) {
                monsterIds.add(p.getMonsterId());
            }
        }
        Map<String, StageLevel> levelMap = new LinkedHashMap<>();
        if (!levelIds.isEmpty()) {
            for (StageLevel lv : stageLevelService.listByIds(levelIds)) {
                if (lv != null) {
                    levelMap.put(lv.getId(), lv);
                }
            }
        }
        Set<String> chapterIds = new LinkedHashSet<>();
        for (StageLevel lv : levelMap.values()) {
            if (lv.getChapterId() != null) {
                chapterIds.add(lv.getChapterId());
            }
        }
        Map<String, StageChapter> chapterMap = new LinkedHashMap<>();
        if (!chapterIds.isEmpty()) {
            for (StageChapter ch : stageChapterService.listByIds(chapterIds)) {
                if (ch != null) {
                    chapterMap.put(ch.getId(), ch);
                }
            }
        }
        Map<String, Monster> monsterMap = new LinkedHashMap<>();
        if (!monsterIds.isEmpty()) {
            for (Monster m : monsterService.listByIds(monsterIds)) {
                if (m != null) {
                    monsterMap.put(m.getId(), m);
                }
            }
        }

        Set<String> seen = new LinkedHashSet<>();
        List<ItemDropSourceVo> result = new ArrayList<>();
        for (StageLevelMonster p : placements) {
            StageLevel level = levelMap.get(p.getLevelId());
            if (level == null || Boolean.FALSE.equals(level.getEnable())) {
                continue;
            }
            MonsterDrop drop = dropByMonster.get(p.getMonsterId());
            if (drop == null) {
                continue;
            }
            String key = level.getId() + ":" + p.getMonsterId();
            if (!seen.add(key)) {
                continue;
            }
            StageChapter chapter = chapterMap.get(level.getChapterId());
            Monster monster = monsterMap.get(p.getMonsterId());
            ItemDropSourceVo vo = new ItemDropSourceVo();
            vo.setLevelId(level.getId());
            vo.setDisplayCode(level.getCode());
            vo.setLevelName(level.getName());
            vo.setChapterName(chapter != null ? chapter.getName() : null);
            vo.setMonsterId(p.getMonsterId());
            vo.setMonsterName(monster != null ? monster.getName() : "怪物");
            vo.setDropRate(drop.getDropRate());
            vo.setMinQty(drop.getMinQty());
            vo.setMaxQty(drop.getMaxQty());
            result.add(vo);
        }
        result.sort((a, b) -> {
            String ca = a.getDisplayCode() == null ? "" : a.getDisplayCode();
            String cb = b.getDisplayCode() == null ? "" : b.getDisplayCode();
            int c = ca.compareTo(cb);
            if (c != 0) {
                return c;
            }
            return String.valueOf(a.getMonsterName()).compareTo(String.valueOf(b.getMonsterName()));
        });
        return result;
    }
}
