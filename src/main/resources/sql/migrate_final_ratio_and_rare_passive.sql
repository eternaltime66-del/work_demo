-- 角色最终攻击/生命/防御比例（最终* 作为高级属性被动效果，不再单独稀有槽）
SET NAMES utf8mb4;

ALTER TABLE app_role_base_stat
  ADD COLUMN final_atk_ratio decimal(20,8) DEFAULT 100 COMMENT '最终攻击比例(单位1%，默认100)' AFTER life_steal_ratio,
  ADD COLUMN final_hp_ratio decimal(20,8) DEFAULT 100 COMMENT '最终生命比例(单位1%，默认100)' AFTER final_atk_ratio,
  ADD COLUMN final_def_ratio decimal(20,8) DEFAULT 100 COMMENT '最终防御比例(单位1%，默认100)' AFTER final_hp_ratio;

ALTER TABLE app_player_role
  ADD COLUMN final_atk_ratio decimal(20,8) DEFAULT 100 COMMENT '最终攻击比例(单位1%，默认100)' AFTER life_steal_ratio,
  ADD COLUMN final_hp_ratio decimal(20,8) DEFAULT 100 COMMENT '最终生命比例(单位1%，默认100)' AFTER final_atk_ratio,
  ADD COLUMN final_def_ratio decimal(20,8) DEFAULT 100 COMMENT '最终防御比例(单位1%，默认100)' AFTER final_hp_ratio;

UPDATE app_role_base_stat SET final_atk_ratio=100 WHERE final_atk_ratio IS NULL OR final_atk_ratio=0;
UPDATE app_role_base_stat SET final_hp_ratio=100 WHERE final_hp_ratio IS NULL OR final_hp_ratio=0;
UPDATE app_role_base_stat SET final_def_ratio=100 WHERE final_def_ratio IS NULL OR final_def_ratio=0;
UPDATE app_player_role SET final_atk_ratio=100 WHERE final_atk_ratio IS NULL OR final_atk_ratio=0;
UPDATE app_player_role SET final_hp_ratio=100 WHERE final_hp_ratio IS NULL OR final_hp_ratio=0;
UPDATE app_player_role SET final_def_ratio=100 WHERE final_def_ratio IS NULL OR final_def_ratio=0;
