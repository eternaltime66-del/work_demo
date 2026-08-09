-- 主动技能战斗内属性时长 buff
-- 若列已存在会报错，可忽略
SET NAMES utf8mb4;

ALTER TABLE app_skill_effect
  ADD COLUMN duration_av int DEFAULT 0 COMMENT '生效行动值(0=永久)' AFTER trigger_rate;

UPDATE app_skill_effect SET duration_av = 0 WHERE duration_av IS NULL;
