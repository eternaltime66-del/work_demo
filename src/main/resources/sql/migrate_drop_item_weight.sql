-- 移除物品重量字段（玩法不再使用）
SET NAMES utf8mb4;

ALTER TABLE app_item DROP COLUMN weight;
