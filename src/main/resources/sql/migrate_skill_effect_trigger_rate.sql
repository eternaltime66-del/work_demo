-- 技能效果 / 被动战斗效果：触发概率（100=100%）
-- 若列已存在会报错，可忽略
SET NAMES utf8mb4;

ALTER TABLE app_skill_effect
  ADD COLUMN trigger_rate int DEFAULT 100 COMMENT '触发概率(100=100%)' AFTER hit_segments;

ALTER TABLE app_passive_combat_effect
  ADD COLUMN trigger_rate int DEFAULT 100 COMMENT '触发概率(100=100%)' AFTER hit_segments;

UPDATE app_skill_effect SET trigger_rate = 100 WHERE trigger_rate IS NULL;
UPDATE app_passive_combat_effect SET trigger_rate = 100 WHERE trigger_rate IS NULL;
