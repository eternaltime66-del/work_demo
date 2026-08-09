-- 攻速参数：武器行动值改为增/减攻速；角色与各装备扩展表增加叠乘字段

-- 武器
ALTER TABLE app_item_weapon
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER base_atk,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

UPDATE app_item_weapon
SET atk_speed_up_ratio = IFNULL(base_action, 0);

ALTER TABLE app_item_weapon DROP COLUMN base_action;

-- 角色模板 / 玩家角色
ALTER TABLE app_role_base_stat
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER life_steal_ratio,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

ALTER TABLE app_player_role
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER life_steal_ratio,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

-- 护甲 / 护手 / 头盔 / 护腿 / 饰品
ALTER TABLE app_item_armor
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER defense,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

ALTER TABLE app_item_gloves
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER defense,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

ALTER TABLE app_item_helmet
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER defense,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

ALTER TABLE app_item_legs
  ADD COLUMN atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)' AFTER defense,
  ADD COLUMN atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)' AFTER atk_speed_up_ratio;

-- 饰品攻速字段已废弃（见 migrate_drop_accessory_atk_speed.sql），此处不再添加
-- ALTER TABLE app_item_accessory
--   ADD COLUMN atk_speed_up_ratio ...

