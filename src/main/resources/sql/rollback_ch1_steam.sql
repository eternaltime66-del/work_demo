-- ============================================================
-- 回滚：移除第一章「蒸汽与齿轮」种子数据
-- 配对文件：seed_ch1_steam.sql
-- 按依赖逆序删除；并清理玩家侧对 CH1 物品/技能的引用
-- 不会动 DEFAULT_NORMAL / 主线 MAIN 数据
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- ---- 0) 玩家运行时引用（避免外键/脏指针）----
UPDATE app_player_equip
SET weapon_item_id = NULL
WHERE weapon_item_id IN ('ITM_CH1_WOOD', 'ITM_CH1_SIPHON');

UPDATE app_player_equip
SET armor_item_id = NULL
WHERE armor_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET gloves_item_id = NULL
WHERE gloves_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET helmet_item_id = NULL
WHERE helmet_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET legs_item_id = NULL
WHERE legs_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET accessory1_item_id = NULL
WHERE accessory1_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET accessory2_item_id = NULL
WHERE accessory2_item_id LIKE 'ITM_CH1_%';

UPDATE app_player_equip
SET accessory3_item_id = NULL
WHERE accessory3_item_id LIKE 'ITM_CH1_%';

DELETE FROM app_warehouse_item WHERE item_id LIKE 'ITM_CH1_%';
DELETE FROM app_battle_bag WHERE item_id LIKE 'ITM_CH1_%';
DELETE FROM app_player_role_skill WHERE skill_id LIKE 'ASK_CH1_%';

-- ---- 1) 关卡链 ----
DELETE FROM app_stage_level_monster WHERE id LIKE 'SLM_CH1_%' OR level_id LIKE 'SLV_CH1_%';
DELETE FROM app_stage WHERE id LIKE 'SLV_CH1_%' OR parent_id = 'SCP_CH1';
DELETE FROM app_stage WHERE id = 'SCP_CH1';

-- ---- 2) 掉落 / 配方 / 装备挂载 / 物品 ----
DELETE FROM app_monster_drop WHERE id LIKE 'MDP_CH1_%' OR monster_id LIKE 'MST_CH1_%';
DELETE FROM app_recipe_material WHERE id LIKE 'RCM_CH1_%' OR recipe_id LIKE 'RCP_CH1_%';
DELETE FROM app_recipe WHERE id LIKE 'RCP_CH1_%';

DELETE FROM app_item_default_skill WHERE id LIKE 'IDS_CH1_%' OR item_id LIKE 'ITM_CH1_%';
DELETE FROM app_item_default_passive WHERE id LIKE 'IDP_CH1_%' OR item_id LIKE 'ITM_CH1_%';
DELETE FROM app_item_weapon WHERE id LIKE 'WPN_CH1_%' OR item_id LIKE 'ITM_CH1_%';
DELETE FROM app_item_material WHERE id LIKE 'MAT_CH1_%' OR item_id LIKE 'ITM_CH1_%';
DELETE FROM app_item WHERE id LIKE 'ITM_CH1_%';

-- ---- 3) 怪物 ----
DELETE FROM app_monster WHERE id LIKE 'MST_CH1_%';

-- ---- 4) 技能输出 / 充能 / 被动 / 主动 / BUFF ----
DELETE FROM app_skill_output
WHERE id LIKE 'SOUT_CH1_%'
   OR skill_id LIKE 'ASK_CH1_%'
   OR passive_skill_id LIKE 'PSK_CH1_%';

DELETE FROM app_skill_charge WHERE id LIKE 'SCH_CH1_%' OR skill_id LIKE 'ASK_CH1_%';
DELETE FROM app_skill_effect WHERE skill_id LIKE 'ASK_CH1_%';

DELETE FROM app_passive_effect WHERE id LIKE 'PSE_CH1_%' OR skill_id LIKE 'PSK_CH1_%';
DELETE FROM app_passive_condition WHERE skill_id LIKE 'PSK_CH1_%';
DELETE FROM app_passive_skill WHERE id LIKE 'PSK_CH1_%';

DELETE FROM app_active_skill WHERE id LIKE 'ASK_CH1_%';

DELETE FROM app_buff_def WHERE id LIKE 'BFD_CH1_%';

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- 完成：CH1 批次已移除
