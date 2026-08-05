-- 武器普攻槽：配置后战斗中替换角色普攻
SET NAMES utf8mb4;

ALTER TABLE app_item_weapon
  ADD COLUMN normal_skill_id varchar(64) DEFAULT NULL COMMENT '普攻技能id（有则替换角色普攻）' AFTER atk_speed_down_ratio;
