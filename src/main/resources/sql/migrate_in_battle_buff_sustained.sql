-- 战斗内属性时长 buff + 持续效果被动槽
-- 若列已存在会报错，可忽略
SET NAMES utf8mb4;

ALTER TABLE app_skill_effect
  ADD COLUMN duration_av int DEFAULT 0 COMMENT '生效行动值(0=永久)' AFTER trigger_rate;

ALTER TABLE app_passive_combat_effect
  ADD COLUMN duration_av int DEFAULT 0 COMMENT '生效行动值(0=永久/持续效果由条件维持)' AFTER trigger_rate;

ALTER TABLE app_item
  ADD COLUMN sustained_passive_slot_count int DEFAULT 0 COMMENT '默认自带持续效果被动数量' AFTER periodic_passive_slot_count,
  ADD COLUMN player_default_edit_sustained_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑持续效果被动数量' AFTER sustained_passive_slot_count,
  ADD COLUMN player_max_edit_sustained_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大持续效果被动数量' AFTER player_default_edit_sustained_passive_slot_count;

UPDATE app_skill_effect SET duration_av = 0 WHERE duration_av IS NULL;
UPDATE app_passive_combat_effect SET duration_av = 0 WHERE duration_av IS NULL;
