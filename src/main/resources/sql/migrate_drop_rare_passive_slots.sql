-- 稀有属性被动已并入高级属性被动，清理独立稀有槽字段与类型
SET NAMES utf8mb4;

UPDATE app_passive_skill SET passive_type='OUT_ADVANCED' WHERE passive_type='OUT_RARE';
UPDATE app_item_default_passive SET passive_type='OUT_ADVANCED' WHERE passive_type='OUT_RARE';

ALTER TABLE app_item
  DROP COLUMN rare_passive_slot_count,
  DROP COLUMN player_default_edit_rare_passive_slot_count,
  DROP COLUMN player_max_edit_rare_passive_slot_count;
