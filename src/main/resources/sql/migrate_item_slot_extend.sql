-- 装备槽位拓展：充能技能槽可编辑数量 + 基础/高级被动槽
SET NAMES utf8mb4;

ALTER TABLE app_item
  MODIFY COLUMN charge_skill_slot_count int DEFAULT 0 COMMENT '默认充能技能槽数量',
  ADD COLUMN player_default_edit_charge_skill_slot_count int DEFAULT 0 COMMENT '玩家默认可编辑充能技能槽数量' AFTER charge_skill_slot_count,
  ADD COLUMN player_max_edit_charge_skill_slot_count int DEFAULT 0 COMMENT '玩家最大可编辑充能技能槽数量' AFTER player_default_edit_charge_skill_slot_count,
  ADD COLUMN basic_passive_slot_count int DEFAULT 0 COMMENT '默认自带基础被动数量' AFTER player_can_edit_skill_slot,
  ADD COLUMN player_default_edit_basic_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑基础被动数量' AFTER basic_passive_slot_count,
  ADD COLUMN player_max_edit_basic_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大基础被动数量' AFTER player_default_edit_basic_passive_slot_count,
  ADD COLUMN advanced_passive_slot_count int DEFAULT 0 COMMENT '默认自带高级属性被动数量' AFTER player_max_edit_basic_passive_slot_count,
  ADD COLUMN player_default_edit_advanced_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑高级属性被动数量' AFTER advanced_passive_slot_count,
  ADD COLUMN player_max_edit_advanced_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大高级属性被动数量' AFTER player_default_edit_advanced_passive_slot_count;
