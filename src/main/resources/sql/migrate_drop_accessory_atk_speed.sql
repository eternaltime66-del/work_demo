-- 饰品去掉面板攻速字段（攻速改由被动等提供）
SET NAMES utf8mb4;

ALTER TABLE app_item_accessory
  DROP COLUMN atk_speed_up_ratio,
  DROP COLUMN atk_speed_down_ratio;
