-- 角色基础攻速（默认 1；行动值 = 100 / 攻速）
ALTER TABLE app_role_base_stat
  ADD COLUMN base_atk_speed decimal(12, 4) DEFAULT 1 COMMENT '基础攻速(默认1)' AFTER base_action;

ALTER TABLE app_player_role
  ADD COLUMN base_atk_speed decimal(12, 4) DEFAULT 1 COMMENT '基础攻速(默认1)' AFTER base_action;

UPDATE app_role_base_stat
SET base_atk_speed = ROUND(100 / base_action, 4)
WHERE base_action IS NOT NULL AND base_action > 0;

UPDATE app_player_role
SET base_atk_speed = ROUND(100 / base_action, 4)
WHERE base_action IS NOT NULL AND base_action > 0;

UPDATE app_role_base_stat
SET base_atk_speed = 1, base_action = 100
WHERE base_atk_speed IS NULL OR base_atk_speed <= 0;

UPDATE app_player_role
SET base_atk_speed = 1, base_action = 100
WHERE base_atk_speed IS NULL OR base_atk_speed <= 0;
