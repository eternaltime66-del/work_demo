-- ============================================================
-- 回滚：霓虹废都 第1~2章 seed_neon_ch12.sql
-- 仅删 NEON_* 前缀数据；不恢复此前被清掉的占位主线章
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

DELETE FROM app_stage_level_monster WHERE id LIKE 'SLM_NEON_%';
DELETE FROM app_player_stage_level WHERE level_id LIKE 'SLV_NEON_%';
DELETE FROM app_player_stage_chapter WHERE chapter_id LIKE 'SCP_NEON_%';
DELETE FROM app_stage WHERE id LIKE 'SLV_NEON_%';
DELETE FROM app_stage WHERE id LIKE 'SCP_NEON_%';

DELETE FROM app_monster_drop WHERE id LIKE 'MDP_NEON_%';
DELETE FROM app_monster WHERE id LIKE 'MST_NEON_%';

DELETE FROM app_recipe_material WHERE id LIKE 'RCM_NEON_%';
DELETE FROM app_recipe WHERE id LIKE 'RCP_NEON_%';

DELETE FROM app_item_default_skill WHERE id LIKE 'IDS_NEON_%';
DELETE FROM app_item_weapon WHERE id LIKE 'WPN_NEON_%';
DELETE FROM app_item_armor WHERE id LIKE 'ARM_NEON_%';
DELETE FROM app_item_material WHERE id LIKE 'MAT_NEON_%';
-- 清玩家持有的霓虹物品（背包/仓库）
DELETE FROM app_battle_bag WHERE item_id LIKE 'ITM_NEON_%';
DELETE FROM app_warehouse_item WHERE item_id LIKE 'ITM_NEON_%';
UPDATE app_player_equip SET
  weapon_item_id = IF(weapon_item_id LIKE 'ITM_NEON_%', NULL, weapon_item_id),
  armor_item_id = IF(armor_item_id LIKE 'ITM_NEON_%', NULL, armor_item_id),
  gloves_item_id = IF(gloves_item_id LIKE 'ITM_NEON_%', NULL, gloves_item_id),
  helmet_item_id = IF(helmet_item_id LIKE 'ITM_NEON_%', NULL, helmet_item_id),
  legs_item_id = IF(legs_item_id LIKE 'ITM_NEON_%', NULL, legs_item_id),
  accessory1_item_id = IF(accessory1_item_id LIKE 'ITM_NEON_%', NULL, accessory1_item_id),
  accessory2_item_id = IF(accessory2_item_id LIKE 'ITM_NEON_%', NULL, accessory2_item_id),
  accessory3_item_id = IF(accessory3_item_id LIKE 'ITM_NEON_%', NULL, accessory3_item_id);
DELETE FROM app_item WHERE id LIKE 'ITM_NEON_%';

DELETE FROM app_skill_output WHERE id LIKE 'SOUT_NEON_%';
DELETE FROM app_skill_charge WHERE id LIKE 'SCH_NEON_%';
DELETE FROM app_active_skill WHERE id LIKE 'ASK_NEON_%';
DELETE FROM app_buff_def WHERE id LIKE 'BFD_NEON_%';

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'rolled_back_neon_ch12' AS ok;
