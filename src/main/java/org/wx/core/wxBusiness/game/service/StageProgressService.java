package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBusiness.game.entity.PlayerStageChapter;
import org.wx.core.wxBusiness.game.entity.PlayerStageLevel;
import org.wx.core.wxBusiness.game.entity.PlayerStamina;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.enums.StageKind;
import org.wx.core.wxBusiness.game.entity.enums.StageModeCode;
import org.wx.core.wxBusiness.game.entity.StageLevelMonster;
import org.wx.core.wxBusiness.game.entity.vo.StageDropPreviewVo;
import org.wx.core.wxBusiness.game.entity.vo.StageFirstRewardVo;
import org.wx.core.wxBusiness.game.entity.vo.StageLevelPreviewVo;
import org.wx.core.wxBusiness.game.entity.vo.StageProgressVo;
import org.wx.core.wxBusiness.game.mapper.PlayerStageLevelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 主线进度：解锁链、每日次数（按 attempt_date 自然日）、体力消耗、首通发奖。
 */
@Service
public class StageProgressService extends WxServiceImpl<PlayerStageLevelMapper, PlayerStageLevel> {

    @Resource
    private StageService stageService;
    @Resource
    private PlayerStaminaService playerStaminaService;
    @Resource
    private PlayerStageChapterService playerStageChapterService;
    @Resource
    private StageFirstRewardService stageFirstRewardService;
    @Resource
    private StageLevelMonsterService stageLevelMonsterService;
    @Resource
    private MonsterDropService monsterDropService;

    public Stage requireModeRoot(String modeCode) {
        Stage root = stageService.find()
                .eq(Stage::getKind, StageKind.TYPE)
                .eq(Stage::getCode, modeCode)
                .one();
        ErrorFactory.throwError(root == null, "未找到模式: " + modeCode);
        return root;
    }

    public Stage resolveTypeRoot(Stage level) {
        Stage chapter = stageService.getById(level.getParentId());
        ErrorFactory.throwError(chapter == null || chapter.getKind() != StageKind.CHAPTER, "小关所属大关无效");
        Stage type = stageService.getById(chapter.getParentId());
        ErrorFactory.throwError(type == null || type.getKind() != StageKind.TYPE, "小关所属模式无效");
        return type;
    }

    /** 模式内全部小关：大关 sort → 小关 sort */
    public List<Stage> listOrderedLevels(String typeId, boolean onlyEnable) {
        List<Stage> out = new ArrayList<>();
        for (Stage chapter : stageService.listChildren(typeId, onlyEnable)) {
            if (chapter.getKind() != StageKind.CHAPTER) {
                continue;
            }
            for (Stage level : stageService.listChildren(chapter.getId(), onlyEnable)) {
                if (level.getKind() == StageKind.LEVEL) {
                    out.add(level);
                }
            }
        }
        return out;
    }

    public PlayerStageLevel getOrCreateLevelProgress(String uid, String levelId) {
        PlayerStageLevel row = this.find()
                .eq(PlayerStageLevel::getUid, uid)
                .eq(PlayerStageLevel::getLevelId, levelId)
                .one();
        if (row != null) {
            return row;
        }
        row = new PlayerStageLevel();
        row.setUid(uid);
        row.setLevelId(levelId);
        row.setCleared(false);
        row.setFirstRewardClaimed(false);
        row.setAttemptCount(0);
        this.save(row);
        return row;
    }

    public int todayAttemptCount(PlayerStageLevel row) {
        if (row == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (row.getAttemptDate() == null || !today.equals(row.getAttemptDate())) {
            return 0;
        }
        return row.getAttemptCount() == null ? 0 : row.getAttemptCount();
    }

    public boolean isLevelCleared(String uid, String levelId) {
        PlayerStageLevel row = this.find()
                .eq(PlayerStageLevel::getUid, uid)
                .eq(PlayerStageLevel::getLevelId, levelId)
                .one();
        return row != null && Boolean.TRUE.equals(row.getCleared());
    }

    public boolean isUnlocked(String uid, Stage level, List<Stage> orderedLevels) {
        if (orderedLevels == null || orderedLevels.isEmpty()) {
            return false;
        }
        if (!level.getId().equals(orderedLevels.get(0).getId())) {
            // find previous
            for (int i = 0; i < orderedLevels.size(); i++) {
                if (level.getId().equals(orderedLevels.get(i).getId())) {
                    if (i == 0) {
                        return true;
                    }
                    return isLevelCleared(uid, orderedLevels.get(i - 1).getId());
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 章节是否已对玩家解锁（章内任一启用小关可进入即可）。
     * 用于配方章节锁定等。
     */
    public boolean isChapterUnlocked(String uid, String chapterId) {
        if (Wx.isEmpty(chapterId)) {
            return true;
        }
        Stage chapter = stageService.getById(chapterId);
        if (chapter == null || chapter.getKind() != StageKind.CHAPTER) {
            return false;
        }
        Stage type = stageService.getById(chapter.getParentId());
        if (type == null || type.getKind() != StageKind.TYPE) {
            return false;
        }
        List<Stage> ordered = listOrderedLevels(type.getId(), true);
        List<Stage> levels = stageService.listChildren(chapterId, true);
        boolean hasLevel = false;
        for (Stage level : levels) {
            if (level == null || level.getKind() != StageKind.LEVEL) {
                continue;
            }
            hasLevel = true;
            if (isUnlocked(uid, level, ordered)) {
                return true;
            }
        }
        // 空章节视为解锁（无小关可挡）
        return !hasLevel;
    }

    /** 批量计算章节解锁，避免合成列表按配方重复扫描整棵关卡树。 */
    public Set<String> listUnlockedChapterIds(String uid, Collection<String> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Set.of();
        }
        List<Stage> requested = stageService.listByIds(chapterIds).stream()
                .filter(ch -> ch != null && ch.getKind() == StageKind.CHAPTER)
                .toList();
        if (requested.isEmpty()) {
            return Set.of();
        }
        Set<String> typeIds = new LinkedHashSet<>();
        for (Stage chapter : requested) {
            if (!Wx.isEmpty(chapter.getParentId())) {
                typeIds.add(chapter.getParentId());
            }
        }
        List<Stage> chapters = stageService.find()
                .in(Stage::getParentId, typeIds)
                .eq(Stage::getKind, StageKind.CHAPTER)
                .eq(Stage::getEnable, true)
                .orderByAsc(Stage::getSort)
                .orderByAsc(Stage::getCode)
                .list();
        Set<String> allChapterIds = new LinkedHashSet<>();
        for (Stage chapter : chapters) {
            allChapterIds.add(chapter.getId());
        }
        List<Stage> levels = allChapterIds.isEmpty() ? List.of() : stageService.find()
                .in(Stage::getParentId, allChapterIds)
                .eq(Stage::getKind, StageKind.LEVEL)
                .eq(Stage::getEnable, true)
                .orderByAsc(Stage::getSort)
                .orderByAsc(Stage::getCode)
                .list();
        Map<String, List<Stage>> levelsByChapter = new HashMap<>();
        for (Stage level : levels) {
            levelsByChapter.computeIfAbsent(level.getParentId(), key -> new ArrayList<>()).add(level);
        }
        Set<String> levelIds = new LinkedHashSet<>();
        for (Stage level : levels) {
            levelIds.add(level.getId());
        }
        Set<String> clearedIds = new LinkedHashSet<>();
        if (!Wx.isEmpty(uid) && !levelIds.isEmpty()) {
            List<PlayerStageLevel> progress = this.find()
                    .eq(PlayerStageLevel::getUid, uid)
                    .in(PlayerStageLevel::getLevelId, levelIds)
                    .eq(PlayerStageLevel::getCleared, true)
                    .list();
            for (PlayerStageLevel row : progress) {
                clearedIds.add(row.getLevelId());
            }
        }
        Set<String> wanted = new LinkedHashSet<>(chapterIds);
        Set<String> unlocked = new LinkedHashSet<>();
        for (String typeId : typeIds) {
            List<Stage> ordered = new ArrayList<>();
            Map<String, Integer> firstIndexByChapter = new HashMap<>();
            for (Stage chapter : chapters) {
                if (!typeId.equals(chapter.getParentId())) {
                    continue;
                }
                List<Stage> chapterLevels = levelsByChapter.getOrDefault(chapter.getId(), List.of());
                firstIndexByChapter.put(chapter.getId(), ordered.size());
                ordered.addAll(chapterLevels);
                if (wanted.contains(chapter.getId()) && chapterLevels.isEmpty()) {
                    unlocked.add(chapter.getId());
                }
            }
            for (Map.Entry<String, Integer> entry : firstIndexByChapter.entrySet()) {
                if (!wanted.contains(entry.getKey())) {
                    continue;
                }
                int index = entry.getValue();
                if (index == 0 || (index > 0 && clearedIds.contains(ordered.get(index - 1).getId()))) {
                    unlocked.add(entry.getKey());
                }
            }
        }
        return unlocked;
    }

    public int fightStaminaCost(Stage level, boolean cleared) {
        if (!cleared) {
            return 0;
        }
        return level.getStaminaCost() == null ? 1 : Math.max(0, level.getStaminaCost());
    }

    /**
     * 主线开战前校验：解锁 / 每日次数 / 扣体力 / 记当日次数。
     */
    @Transactional(rollbackFor = Exception.class)
    public PlayerStamina prepareMainlineFight(String uid, Stage level) {
        Stage type = resolveTypeRoot(level);
        ErrorFactory.throwError(!StageModeCode.MAIN.equals(type.getCode()), "该关卡请使用对应模式入口");
        ErrorFactory.throwError(Boolean.FALSE.equals(level.getEnable()), "关卡未启用");

        List<Stage> ordered = listOrderedLevels(type.getId(), true);
        ErrorFactory.throwError(!isUnlocked(uid, level, ordered), "关卡未解锁，请先通关上一关");

        PlayerStageLevel progress = getOrCreateLevelProgress(uid, level.getId());
        boolean cleared = Boolean.TRUE.equals(progress.getCleared());

        LocalDate today = LocalDate.now();
        int todayCount = todayAttemptCount(progress);
        Integer max = level.getDailyMaxAttempts();
        if (max != null && max > 0) {
            ErrorFactory.throwError(todayCount >= max, "今日攻打次数已达上限");
        }

        int cost = fightStaminaCost(level, cleared);
        PlayerStamina stamina = playerStaminaService.consume(uid, cost);

        if (progress.getAttemptDate() == null || !today.equals(progress.getAttemptDate())) {
            progress.setAttemptDate(today);
            progress.setAttemptCount(1);
        } else {
            progress.setAttemptCount(todayCount + 1);
        }
        this.updateById(progress);
        return stamina;
    }

    @Transactional(rollbackFor = Exception.class)
    public MainlineWinReward onMainlineWin(String uid, Stage level) {
        MainlineWinReward reward = new MainlineWinReward();
        PlayerStageLevel progress = getOrCreateLevelProgress(uid, level.getId());
        boolean firstClear = !Boolean.TRUE.equals(progress.getCleared());
        if (firstClear) {
            progress.setCleared(true);
            // 首通才结算首通奖励（未配置则为空列表，前端显示「无」）
            if (!Boolean.TRUE.equals(progress.getFirstRewardClaimed())) {
                List<StageFirstRewardVo> granted = stageFirstRewardService.grantToWarehouse(uid, level.getId());
                progress.setFirstRewardClaimed(true);
                reward.setLevelFirstRewards(granted);
            }
        }
        this.updateById(progress);

        String chapterId = level.getParentId();
        if (!Wx.isEmpty(chapterId)) {
            boolean allCleared = true;
            List<Stage> levels = stageService.listChildren(chapterId, true).stream()
                    .filter(s -> s.getKind() == StageKind.LEVEL)
                    .toList();
            for (Stage lv : levels) {
                if (!isLevelCleared(uid, lv.getId())) {
                    allCleared = false;
                    break;
                }
            }
            if (allCleared) {
                PlayerStageChapter ch = playerStageChapterService.getOrCreate(uid, chapterId);
                if (!Boolean.TRUE.equals(ch.getCleared())) {
                    ch.setCleared(true);
                }
                if (!Boolean.TRUE.equals(ch.getFirstRewardClaimed())) {
                    List<StageFirstRewardVo> granted = stageFirstRewardService.grantToWarehouse(uid, chapterId);
                    ch.setFirstRewardClaimed(true);
                    reward.setChapterFirstRewards(granted);
                }
                playerStageChapterService.updateById(ch);
            }
        }
        reward.setFirstClear(firstClear);
        return reward;
    }

    /** 关卡预览：首通状态 / 首通奖励 / 怪物掉落清单 */
    public StageLevelPreviewVo buildLevelPreview(String uid, String levelId) {
        Stage level = stageService.requireLevel(levelId);
        ErrorFactory.throwError(Boolean.FALSE.equals(level.getEnable()), "关卡未启用");

        Stage type = resolveTypeRoot(level);
        List<Stage> ordered = listOrderedLevels(type.getId(), true);
        PlayerStageLevel progress = null;
        if (!Wx.isEmpty(uid)) {
            progress = this.find()
                    .eq(PlayerStageLevel::getUid, uid)
                    .eq(PlayerStageLevel::getLevelId, levelId)
                    .one();
        }
        boolean cleared = progress != null && Boolean.TRUE.equals(progress.getCleared());
        boolean claimed = progress != null && Boolean.TRUE.equals(progress.getFirstRewardClaimed());
        int today = todayAttemptCount(progress);
        Integer max = level.getDailyMaxAttempts();

        StageLevelPreviewVo vo = new StageLevelPreviewVo();
        vo.setLevelId(level.getId());
        vo.setName(level.getName());
        vo.setCode(level.getCode());
        vo.setCleared(cleared);
        vo.setFirstRewardClaimed(claimed);
        vo.setUnlocked(isUnlocked(uid, level, ordered));
        vo.setFightStaminaCost(fightStaminaCost(level, cleared));
        vo.setDailyMaxAttempts(max);
        vo.setTodayAttemptCount(today);
        vo.setTodayAttemptLeft(max == null || max <= 0 ? null : Math.max(0, max - today));
        vo.setFirstRewards(stageFirstRewardService.listVoByStageId(level.getId()));

        Set<String> monsterIds = new LinkedHashSet<>();
        for (StageLevelMonster sm : stageLevelMonsterService.listByLevelId(levelId)) {
            if (sm != null && !Wx.isEmpty(sm.getMonsterId())) {
                monsterIds.add(sm.getMonsterId());
            }
        }
        List<StageDropPreviewVo> drops = monsterDropService.catalogPreviewByMonsterIds(monsterIds);
        vo.setDrops(drops);
        return vo;
    }

    public StageProgressVo buildMainProgressTree(String uid) {
        Stage root = requireModeRoot(StageModeCode.MAIN);
        List<Stage> ordered = listOrderedLevels(root.getId(), true);
        Map<String, PlayerStageLevel> progressMap = new HashMap<>();
        if (!Wx.isEmpty(uid)) {
            List<PlayerStageLevel> rows = this.find().eq(PlayerStageLevel::getUid, uid).list();
            for (PlayerStageLevel row : rows) {
                progressMap.put(row.getLevelId(), row);
            }
        }
        Map<String, PlayerStageChapter> chapterMap = new HashMap<>();
        if (!Wx.isEmpty(uid)) {
            for (PlayerStageChapter ch : playerStageChapterService.listByUid(uid)) {
                chapterMap.put(ch.getChapterId(), ch);
            }
        }

        StageProgressVo typeVo = toBaseVo(root);
        typeVo.setUnlocked(true);
        typeVo.setCleared(false);
        List<StageProgressVo> chapters = new ArrayList<>();
        for (Stage chapter : stageService.listChildren(root.getId(), true)) {
            if (chapter.getKind() != StageKind.CHAPTER) {
                continue;
            }
            StageProgressVo chVo = toBaseVo(chapter);
            PlayerStageChapter psc = chapterMap.get(chapter.getId());
            chVo.setCleared(psc != null && Boolean.TRUE.equals(psc.getCleared()));
            chVo.setFirstRewardClaimed(psc != null && Boolean.TRUE.equals(psc.getFirstRewardClaimed()));
            chVo.setFirstRewards(stageFirstRewardService.listVoByStageId(chapter.getId()));
            List<StageProgressVo> levels = new ArrayList<>();
            boolean chapterUnlocked = false;
            for (Stage level : stageService.listChildren(chapter.getId(), true)) {
                if (level.getKind() != StageKind.LEVEL) {
                    continue;
                }
                StageProgressVo lvVo = toBaseVo(level);
                PlayerStageLevel psl = progressMap.get(level.getId());
                boolean cleared = psl != null && Boolean.TRUE.equals(psl.getCleared());
                boolean unlocked = isUnlocked(uid, level, ordered);
                if (unlocked) {
                    chapterUnlocked = true;
                }
                int today = todayAttemptCount(psl);
                Integer max = level.getDailyMaxAttempts();
                lvVo.setUnlocked(unlocked);
                lvVo.setCleared(cleared);
                lvVo.setFirstRewardClaimed(psl != null && Boolean.TRUE.equals(psl.getFirstRewardClaimed()));
                lvVo.setFightStaminaCost(fightStaminaCost(level, cleared));
                lvVo.setTodayAttemptCount(today);
                lvVo.setTodayAttemptLeft(max == null || max <= 0 ? null : Math.max(0, max - today));
                lvVo.setFirstRewards(stageFirstRewardService.listVoByStageId(level.getId()));
                levels.add(lvVo);
            }
            chVo.setUnlocked(chapterUnlocked || levels.isEmpty());
            chVo.setLevels(levels);
            chapters.add(chVo);
        }
        typeVo.setChapters(chapters);
        return typeVo;
    }

    private StageProgressVo toBaseVo(Stage s) {
        StageProgressVo vo = new StageProgressVo();
        vo.setId(s.getId());
        vo.setParentId(s.getParentId());
        vo.setKind(s.getKind() != null ? s.getKind().name() : null);
        vo.setName(s.getName());
        vo.setCode(s.getCode());
        vo.setSort(s.getSort());
        vo.setEnable(s.getEnable());
        vo.setStaminaCost(s.getStaminaCost());
        vo.setDailyMaxAttempts(s.getDailyMaxAttempts());
        return vo;
    }

    public static class MainlineWinReward {
        private boolean firstClear;
        private List<StageFirstRewardVo> levelFirstRewards = new ArrayList<>();
        private List<StageFirstRewardVo> chapterFirstRewards = new ArrayList<>();

        public boolean isFirstClear() {
            return firstClear;
        }

        public void setFirstClear(boolean firstClear) {
            this.firstClear = firstClear;
        }

        public List<StageFirstRewardVo> getLevelFirstRewards() {
            return levelFirstRewards;
        }

        public void setLevelFirstRewards(List<StageFirstRewardVo> levelFirstRewards) {
            this.levelFirstRewards = levelFirstRewards != null ? levelFirstRewards : new ArrayList<>();
        }

        public List<StageFirstRewardVo> getChapterFirstRewards() {
            return chapterFirstRewards;
        }

        public void setChapterFirstRewards(List<StageFirstRewardVo> chapterFirstRewards) {
            this.chapterFirstRewards = chapterFirstRewards != null ? chapterFirstRewards : new ArrayList<>();
        }
    }
}
