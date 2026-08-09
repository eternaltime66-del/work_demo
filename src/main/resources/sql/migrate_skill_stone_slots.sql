-- 技能石：作为特殊装备装入 8 个技能槽，通过 app_item_default_skill 提供主动技能。
SET NAMES utf8mb4;

ALTER TABLE app_player_equip
  ADD COLUMN skill1_item_id VARCHAR(64) NULL AFTER accessory3_item_id,
  ADD COLUMN skill2_item_id VARCHAR(64) NULL AFTER skill1_item_id,
  ADD COLUMN skill3_item_id VARCHAR(64) NULL AFTER skill2_item_id,
  ADD COLUMN skill4_item_id VARCHAR(64) NULL AFTER skill3_item_id,
  ADD COLUMN skill5_item_id VARCHAR(64) NULL AFTER skill4_item_id,
  ADD COLUMN skill6_item_id VARCHAR(64) NULL AFTER skill5_item_id,
  ADD COLUMN skill7_item_id VARCHAR(64) NULL AFTER skill6_item_id,
  ADD COLUMN skill8_item_id VARCHAR(64) NULL AFTER skill7_item_id;
