-- 周期计算被动字段 + 装备周期被动槽
SET NAMES utf8mb4;

ALTER TABLE app_passive_skill
  ADD COLUMN periodic_trigger_mode varchar(32) DEFAULT NULL COMMENT 'IN_PERIODIC: SELF/ANY/ANY_ENEMY/ANY_ALLY/FORMULA' AFTER ref_skill_id,
  ADD COLUMN left_formula_json longtext COMMENT '周期触发-上公式' AFTER periodic_trigger_mode,
  ADD COLUMN compare_op varchar(16) DEFAULT NULL COMMENT '周期触发比较符' AFTER left_formula_json,
  ADD COLUMN right_formula_json longtext COMMENT '周期触发-下公式' AFTER compare_op,
  ADD COLUMN max_trigger_per_battle int DEFAULT 0 COMMENT '本场最多触发次数，0/NULL=不限' AFTER right_formula_json;

ALTER TABLE app_item
  ADD COLUMN periodic_passive_slot_count int DEFAULT 0 COMMENT '默认自带周期被动数量' AFTER player_max_edit_anchor_passive_slot_count,
  ADD COLUMN player_default_edit_periodic_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑周期被动数量' AFTER periodic_passive_slot_count,
  ADD COLUMN player_max_edit_periodic_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大周期被动数量' AFTER player_default_edit_periodic_passive_slot_count;
