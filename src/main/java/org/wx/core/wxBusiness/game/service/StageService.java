package org.wx.core.wxBusiness.game.service;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxServiceImpl;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBase.unit.BizIdUtil;
import org.wx.core.wxBusiness.game.entity.Stage;
import org.wx.core.wxBusiness.game.entity.StageFirstReward;
import org.wx.core.wxBusiness.game.entity.enums.StageKind;
import org.wx.core.wxBusiness.game.mapper.StageMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StageService extends WxServiceImpl<StageMapper, Stage> {

    @Lazy
    @Resource
    private StageLevelMonsterService stageLevelMonsterService;
    @Lazy
    @Resource
    private StageFirstRewardService stageFirstRewardService;

    public List<Stage> listChildren(String parentId, boolean onlyEnable) {
        if (Wx.isEmpty(parentId)) {
            return listByKind(StageKind.TYPE, onlyEnable);
        }
        var q = this.find().eq(Stage::getParentId, parentId);
        if (onlyEnable) {
            q.eq(Stage::getEnable, true);
        }
        return q.orderByAsc(Stage::getSort).orderByAsc(Stage::getCode).list();
    }

    public List<Stage> listByKind(StageKind kind, boolean onlyEnable) {
        var q = this.find().eq(Stage::getKind, kind);
        if (onlyEnable) {
            q.eq(Stage::getEnable, true);
        }
        return q.orderByAsc(Stage::getSort).orderByAsc(Stage::getCode).list();
    }

    public Stage requireLevel(String levelId) {
        ErrorFactory.throwError(Wx.isEmpty(levelId), "小关不能为空");
        Stage level = this.getById(levelId);
        ErrorFactory.throwError(level == null, "小关不存在");
        ErrorFactory.throwError(level.getKind() != StageKind.LEVEL, "节点不是小关");
        return level;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveNode(Stage entity) {
        ErrorFactory.throwError(entity == null, "关卡不能为空");
        ErrorFactory.throwError(entity.getKind() == null, "节点类型不能为空");
        ErrorFactory.throwError(Wx.isEmpty(entity.getName()), "名称不能为空");

        applyCompatParent(entity);
        normalizeParent(entity);
        validateHierarchy(entity);

        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnable() == null) {
            entity.setEnable(true);
        }
        if (entity.getKind() == StageKind.LEVEL && entity.getStaminaCost() == null) {
            entity.setStaminaCost(1);
        }
        if (Wx.isEmpty(entity.getId())) {
            entity.setId(BizIdUtil.next(idPrefix(entity.getKind())));
        }
        this.saveOrUpdate(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeCascade(String id) {
        ErrorFactory.throwError(Wx.isEmpty(id), "id 不能为空");
        Stage node = this.getById(id);
        if (node == null) {
            return;
        }
        List<Stage> children = this.find().eq(Stage::getParentId, id).list();
        for (Stage child : children) {
            removeCascade(child.getId());
        }
        if (node.getKind() == StageKind.LEVEL) {
            stageLevelMonsterService.removeByLevelId(id);
        }
        if (node.getKind() == StageKind.LEVEL || node.getKind() == StageKind.CHAPTER) {
            stageFirstRewardService.remove(
                    stageFirstRewardService.find()
                            .eq(StageFirstReward::getStageId, id)
                            .wrapper());
        }
        this.removeById(id);
    }

    /**
     * 兼容前台树：types[].chapters[].levels
     */
    public List<Map<String, Object>> buildLegacyTree(boolean onlyEnable) {
        List<Stage> roots = listChildren(null, onlyEnable);
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Stage type : roots) {
            if (type.getKind() != StageKind.TYPE) {
                continue;
            }
            Map<String, Object> typeNode = toNodeMap(type);
            List<Map<String, Object>> chapterNodes = new ArrayList<>();
            for (Stage chapter : listChildren(type.getId(), onlyEnable)) {
                if (chapter.getKind() != StageKind.CHAPTER) {
                    continue;
                }
                Map<String, Object> chapterNode = toNodeMap(chapter);
                chapterNode.put("typeId", chapter.getParentId());
                List<Stage> levels = listChildren(chapter.getId(), onlyEnable).stream()
                        .filter(s -> s.getKind() == StageKind.LEVEL)
                        .toList();
                chapterNode.put("levels", levels);
                chapterNodes.add(chapterNode);
            }
            typeNode.put("chapters", chapterNodes);
            tree.add(typeNode);
        }
        return tree;
    }

    private Map<String, Object> toNodeMap(Stage s) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("parentId", s.getParentId());
        m.put("kind", s.getKind());
        m.put("name", s.getName());
        m.put("code", s.getCode());
        m.put("sort", s.getSort() != null ? s.getSort() : 0);
        m.put("enable", s.getEnable());
        m.put("remark", s.getRemark());
        m.put("staminaCost", s.getStaminaCost());
        m.put("dailyMaxAttempts", s.getDailyMaxAttempts());
        return m;
    }

    private void applyCompatParent(Stage entity) {
        if (!Wx.isEmpty(entity.getParentId())) {
            return;
        }
        if (entity.getKind() == StageKind.CHAPTER && !Wx.isEmpty(entity.getTypeId())) {
            entity.setParentId(entity.getTypeId());
        } else if (entity.getKind() == StageKind.LEVEL && !Wx.isEmpty(entity.getChapterId())) {
            entity.setParentId(entity.getChapterId());
        }
    }

    private void normalizeParent(Stage entity) {
        if (entity.getKind() == StageKind.TYPE) {
            entity.setParentId(null);
            return;
        }
        ErrorFactory.throwError(Wx.isEmpty(entity.getParentId()), "父节点不能为空");
    }

    private void validateHierarchy(Stage entity) {
        if (entity.getKind() == StageKind.TYPE) {
            return;
        }
        Stage parent = this.getById(entity.getParentId());
        ErrorFactory.throwError(parent == null, "父节点不存在");
        if (entity.getKind() == StageKind.CHAPTER) {
            ErrorFactory.throwError(parent.getKind() != StageKind.TYPE, "大关的父节点必须是分类");
        } else if (entity.getKind() == StageKind.LEVEL) {
            ErrorFactory.throwError(parent.getKind() != StageKind.CHAPTER, "小关的父节点必须是大关");
        }
        if (!Wx.isEmpty(entity.getId())) {
            ErrorFactory.throwError(entity.getId().equals(entity.getParentId()), "不能将自身设为父节点");
        }
    }

    private static String idPrefix(StageKind kind) {
        return switch (kind) {
            case TYPE -> "STY";
            case CHAPTER -> "SCP";
            case LEVEL -> "SLV";
        };
    }
}
