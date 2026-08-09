-- ============================================================
-- 霓虹废都 第1~2章（表格版落地）
-- 决策摘要：
--   · 数值以完整合集表格为准；攻击间隔数字=base_action(AV)
--   · 技能充能以表末 3/8；位移/经验/侵蚀描述忽略
--   · 闪避=BuffKind.DODGE；配方按章节解锁
-- 依赖：STY_10000001、DEFAULT_NORMAL(ASK_10000001)
-- 回滚：rollback_neon_ch12.sql
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

SET @F_ATK_1  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1"}]';
SET @F_ATK_12 = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1.2"}]';
SET @F_ATK_15 = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1.5"}]';
SET @F_ATK_08 = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"0.8"}]';
SET @F_ATK_2  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"2"}]';
SET @F_LIT_20 = '[{"kind":"PARAM","paramMode":"LITERAL","value":"20"}]';

-- 清占位主线章关（若存在）
DELETE FROM app_stage_level_monster WHERE level_id IN (
  SELECT id FROM (SELECT id FROM app_stage WHERE parent_id IN (
    SELECT id FROM app_stage WHERE parent_id='STY_10000001' AND kind='CHAPTER' AND id NOT LIKE 'SCP_NEON%'
  )) t
);
DELETE FROM app_player_stage_level WHERE level_id IN (
  SELECT id FROM (SELECT id FROM app_stage WHERE parent_id IN (
    SELECT id FROM app_stage WHERE parent_id='STY_10000001' AND kind='CHAPTER' AND id NOT LIKE 'SCP_NEON%'
  )) t
);
DELETE FROM app_player_stage_chapter WHERE chapter_id IN (
  SELECT id FROM (SELECT id FROM app_stage WHERE parent_id='STY_10000001' AND kind='CHAPTER' AND id NOT LIKE 'SCP_NEON%') t
);
DELETE FROM app_stage WHERE parent_id IN (
  SELECT id FROM (SELECT id FROM app_stage WHERE parent_id='STY_10000001' AND kind='CHAPTER' AND id NOT LIKE 'SCP_NEON%') t
);
DELETE FROM app_stage WHERE parent_id='STY_10000001' AND kind='CHAPTER' AND id NOT LIKE 'SCP_NEON%';

-- ============================================================
-- Buff：能量突闪 → 闪避
-- ============================================================
INSERT INTO app_buff_def (
  id, name, code, buff_kind, beneficial, dispelable, stack_mode, max_stacks, duration_av,
  dodge_chance, skill_match_mode, match_skill_school, match_skill_type, match_damage_element, match_skill_id,
  sort, enable, remark, create_time, update_time
) VALUES
('BFD_NEON_DODGE', '能量突闪·闪避', 'NEON_DODGE', 'DODGE', 1, 1, 'NONE', 0, 100,
 45, 'ANY', NULL, NULL, NULL, NULL,
 10, 1, '二章自保：持有期间闪避45%', NOW(), NOW())
ON DUPLICATE KEY UPDATE dodge_chance=VALUES(dodge_chance), duration_av=VALUES(duration_av), buff_kind='DODGE';

INSERT INTO app_buff_def (
  id, name, code, buff_kind, beneficial, dispelable, stack_mode, max_stacks, duration_av,
  attr_key, attr_dir, formula_json, sort, enable, remark, create_time, update_time
) VALUES
('BFD_NEON_ATKUP', '凝光一瞬', 'NEON_ATKUP', 'ATTR', 1, 1, 'NONE', 0, 100,
 'FINAL_ATK', 'INCREASE', @F_LIT_20, 11, 1, '浮晶大招：短时加攻', NOW(), NOW())
ON DUPLICATE KEY UPDATE formula_json=VALUES(formula_json);

-- ============================================================
-- 技能
-- ============================================================
INSERT INTO app_active_skill (
  id, name, skill_type, skill_school, damage_element, code,
  need_charge_mode, need_charge, max_cast_skill, max_cast_global, max_cast_all_means, max_cast_role,
  sort, enable, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('ASK_NEON_CRY_S', '漏电溅射', 'SMALL', '无', 'SHOCK', 'NEON_CRY_S',
 'MANUAL', 24, 0, 0, 0, 0, 22, 1, '漏电浮晶小技能·周身', NOW(), NOW()),
('ASK_NEON_CRY_U', '凝光一瞬', 'ULTIMATE', '无', 'PHYSICAL', 'NEON_CRY_U',
 'MANUAL', 40, 0, 0, 0, 0, 23, 1, '漏电浮晶大招·加攻', NOW(), NOW()),
('ASK_NEON_BOSS_S', '散晶落影', 'SMALL', '无', 'PHYSICAL', 'NEON_BOSS_S',
 'MANUAL', 36, 0, 0, 0, 0, 25, 1, '晶核兽小技能·范围', NOW(), NOW()),
('ASK_NEON_BOSS_U', '核光震荡', 'ULTIMATE', '无', 'PHYSICAL', 'NEON_BOSS_U',
 'MANUAL', 60, 0, 0, 0, 0, 26, 1, '晶核兽大招·高伤（可闪避）', NOW(), NOW()),
('ASK_NEON_SWEEP', '光刃横扫', 'SMALL', '无', 'PHYSICAL', 'NEON_SWEEP',
 'MANUAL', 3, 0, 0, 0, 0, 27, 1, '微光切割刃·范围', NOW(), NOW()),
('ASK_NEON_FLASH', '能量突闪', 'SMALL', '无', 'PHYSICAL', 'NEON_FLASH',
 'MANUAL', 8, 0, 0, 0, 0, 28, 1, '自保·挂闪避BUFF', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), need_charge=VALUES(need_charge);

-- 充能
INSERT INTO app_skill_charge (
  id, skill_id, name, condition_type, scope, every_action_value, charge_gain,
  skill_charge_event, skill_charge_match, match_skill_type, sort, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('SCH_NEON_CRY_S', 'ASK_NEON_CRY_S', '行动充能', 'ACTION_VALUE', 'GLOBAL', 120, 24,
 NULL, NULL, NULL, 0, NULL, NOW(), NOW()),
('SCH_NEON_CRY_U', 'ASK_NEON_CRY_U', '行动充能', 'ACTION_VALUE', 'GLOBAL', 200, 40,
 NULL, NULL, NULL, 0, NULL, NOW(), NOW()),
('SCH_NEON_BOSS_S', 'ASK_NEON_BOSS_S', '行动充能', 'ACTION_VALUE', 'GLOBAL', 180, 36,
 NULL, NULL, NULL, 0, NULL, NOW(), NOW()),
('SCH_NEON_BOSS_U', 'ASK_NEON_BOSS_U', '行动充能', 'ACTION_VALUE', 'GLOBAL', 300, 60,
 NULL, NULL, NULL, 0, NULL, NOW(), NOW()),
('SCH_NEON_SWEEP', 'ASK_NEON_SWEEP', '普攻充能', 'SKILL_CHARGE', 'GLOBAL', NULL, 1,
 'CAST', 'ANY_TYPE', 'NORMAL', 0, '表末：普攻+1，所需3', NOW(), NOW()),
('SCH_NEON_FLASH1', 'ASK_NEON_FLASH', '普攻充能', 'SKILL_CHARGE', 'GLOBAL', NULL, 1,
 'CAST', 'ANY_TYPE', 'NORMAL', 0, '表末：普攻+1', NOW(), NOW()),
('SCH_NEON_FLASH2', 'ASK_NEON_FLASH', '受伤充能', 'SKILL_CHARGE', 'GLOBAL', NULL, 1,
 'TAKE_DAMAGE', 'ANY', NULL, 1, '表末：受技+1', NOW(), NOW())
ON DUPLICATE KEY UPDATE charge_gain=VALUES(charge_gain);

-- 输出
DELETE FROM app_skill_output WHERE id LIKE 'SOUT_NEON_%';
INSERT INTO app_skill_output (
  id, skill_id, name, output_kind, target_type, effect_type, damage_element,
  formula_json, hit_segments, trigger_rate, duration_av, buff_def_id, sort, create_time, update_time
) VALUES
('SOUT_NEON_CRY_S', 'ASK_NEON_CRY_S', '漏电溅射', 'EFFECT', 'ALL_ENEMY', 'DAMAGE', 'SHOCK', @F_ATK_1, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_NEON_CRY_U', 'ASK_NEON_CRY_U', '凝光一瞬', 'APPEND_BUFF', 'SELF', NULL, 'PHYSICAL', NULL, 1, 100, 0, 'BFD_NEON_ATKUP', 0, NOW(), NOW()),
('SOUT_NEON_BOSS_S', 'ASK_NEON_BOSS_S', '散晶落影', 'EFFECT', 'ALL_ENEMY', 'DAMAGE', 'PHYSICAL', @F_ATK_12, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_NEON_BOSS_U', 'ASK_NEON_BOSS_U', '核光震荡', 'EFFECT', 'ALL_ENEMY', 'DAMAGE', 'PHYSICAL', @F_ATK_2, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_NEON_SWEEP', 'ASK_NEON_SWEEP', '光刃横扫', 'EFFECT', 'ALL_ENEMY', 'DAMAGE', 'PHYSICAL', @F_ATK_12, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_NEON_FLASH', 'ASK_NEON_FLASH', '能量突闪', 'APPEND_BUFF', 'SELF', NULL, 'PHYSICAL', NULL, 1, 100, 0, 'BFD_NEON_DODGE', 0, NOW(), NOW());

-- ============================================================
-- 物品 / 材料 / 装备
-- ============================================================
INSERT INTO app_item (
  id, code, name, icon, item_type, max_stack, sort, enable, remark,
  charge_skill_slot_count, player_default_edit_charge_skill_slot_count, player_max_edit_charge_skill_slot_count,
  basic_passive_slot_count, advanced_passive_slot_count, battle_combat_passive_slot_count,
  CREATE_TIME, UPDATE_TIME
) VALUES
('ITM_NEON_FLUFF', 'neon_fluff', '絮光软绒', '/art/item/neon_fluff.png', 'MATERIAL', 99, 1001, 1, '微光絮虫掉落', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_FILM', 'neon_film', '泡囊光膜', '/art/item/neon_film.png', 'MATERIAL', 99, 1002, 1, '电路软泡掉落', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_DUST', 'neon_dust', '银耀光晶屑', '/art/item/neon_dust.png', 'MATERIAL', 99, 1003, 1, '光尘飞蚁掉落', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_GRAIN', 'neon_grain', '紊流晶粒', '/art/item/neon_grain.png', 'MATERIAL', 99, 1004, 1, '漏电浮晶掉落', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_CORE', 'neon_core', '凝光核芯', '/art/item/neon_core.png', 'MATERIAL', 99, 1005, 1, '晶核兽掉落', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_BLADE1', 'neon_pulse_blade', '絮光脉冲刃', '/art/item/neon_pulse_blade.png', 'WEAPON', 1, 1101, 1, '一章合成武器 攻+4', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_ARMOR1', 'neon_street_armor', '街区绝缘软甲', '/art/item/neon_street_armor.png', 'ARMOR', 1, 1102, 1, '一章合成护甲', 0,0,0,0,0,0, NOW(), NOW()),
('ITM_NEON_BLADE2', 'neon_cut_blade', '微光切割刃', '/art/item/neon_cut_blade.png', 'WEAPON', 1, 1103, 1, '二章合成武器 攻+8 攻速+0.1', 2,0,2,0,0,0, NOW(), NOW()),
('ITM_NEON_ARMOR2', 'neon_glow_liner', '荧光防护内衬', '/art/item/neon_glow_liner.png', 'ARMOR', 1, 1104, 1, '二章合成护甲', 0,0,0,0,0,0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), icon=VALUES(icon), enable=1;

INSERT INTO app_item_material (id, item_id, grade, CREATE_TIME, UPDATE_TIME) VALUES
('MAT_NEON_FLUFF', 'ITM_NEON_FLUFF', 1, NOW(), NOW()),
('MAT_NEON_FILM', 'ITM_NEON_FILM', 1, NOW(), NOW()),
('MAT_NEON_DUST', 'ITM_NEON_DUST', 1, NOW(), NOW()),
('MAT_NEON_GRAIN', 'ITM_NEON_GRAIN', 2, NOW(), NOW()),
('MAT_NEON_CORE', 'ITM_NEON_CORE', 3, NOW(), NOW())
ON DUPLICATE KEY UPDATE grade=VALUES(grade);

INSERT INTO app_item_weapon (id, item_id, base_atk, atk_speed_up_ratio, atk_speed_down_ratio, normal_skill_id, remark, CREATE_TIME, UPDATE_TIME) VALUES
('WPN_NEON_BLADE1', 'ITM_NEON_BLADE1', 4, 0, 0, NULL, '默认普攻', NOW(), NOW()),
('WPN_NEON_BLADE2', 'ITM_NEON_BLADE2', 8, 0.1000, 0, NULL, '绑光刃横扫+能量突闪', NOW(), NOW())
ON DUPLICATE KEY UPDATE base_atk=VALUES(base_atk), atk_speed_up_ratio=VALUES(atk_speed_up_ratio);

INSERT INTO app_item_armor (id, item_id, hp, defense, atk_speed_up_ratio, atk_speed_down_ratio, remark, CREATE_TIME, UPDATE_TIME) VALUES
('ARM_NEON_1', 'ITM_NEON_ARMOR1', 30, 3, NULL, NULL, '一章软甲', NOW(), NOW()),
('ARM_NEON_2', 'ITM_NEON_ARMOR2', 50, 6, NULL, NULL, '二章内衬', NOW(), NOW())
ON DUPLICATE KEY UPDATE hp=VALUES(hp), defense=VALUES(defense);

DELETE FROM app_item_default_skill WHERE id LIKE 'IDS_NEON_%';
INSERT INTO app_item_default_skill (id, item_id, skill_id, slot_no, sort, CREATE_TIME, UPDATE_TIME) VALUES
('IDS_NEON_SWEEP', 'ITM_NEON_BLADE2', 'ASK_NEON_SWEEP', 0, 0, NOW(), NOW()),
('IDS_NEON_FLASH', 'ITM_NEON_BLADE2', 'ASK_NEON_FLASH', 1, 1, NOW(), NOW());

-- ============================================================
-- 章节（先插入，配方锁章用）
-- ============================================================
INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, stamina_cost, remark, CREATE_TIME, UPDATE_TIME) VALUES
('SCP_NEON_01', 'STY_10000001', 'CHAPTER', '新手街区', 'NEON_CH1', 1, 1, NULL, '霓虹废都第一章', NOW(), NOW()),
('SCP_NEON_02', 'STY_10000001', 'CHAPTER', '微光隘口', 'NEON_CH2', 2, 1, NULL, '霓虹废都第二章', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), sort=VALUES(sort), enable=1;

-- 配方（进入对应章解锁）
INSERT INTO app_recipe (id, name, output_item_id, output_qty, sort, enable, unlock_chapter_id, remark, CREATE_TIME, UPDATE_TIME) VALUES
('RCP_NEON_BLADE1', '絮光脉冲刃', 'ITM_NEON_BLADE1', 1, 1001, 1, 'SCP_NEON_01', '软绒4+光膜2+晶屑6', NOW(), NOW()),
('RCP_NEON_ARMOR1', '街区绝缘软甲', 'ITM_NEON_ARMOR1', 1, 1002, 1, 'SCP_NEON_01', '软绒6+光膜5+晶屑3', NOW(), NOW()),
('RCP_NEON_BLADE2', '微光切割刃', 'ITM_NEON_BLADE2', 1, 1003, 1, 'SCP_NEON_02', '晶粒等；进二章解锁', NOW(), NOW()),
('RCP_NEON_ARMOR2', '荧光防护内衬', 'ITM_NEON_ARMOR2', 1, 1004, 1, 'SCP_NEON_02', '核芯等；进二章解锁', NOW(), NOW())
ON DUPLICATE KEY UPDATE unlock_chapter_id=VALUES(unlock_chapter_id), enable=1;

DELETE FROM app_recipe_material WHERE id LIKE 'RCM_NEON_%';
INSERT INTO app_recipe_material (id, recipe_id, item_id, quantity, sort, CREATE_TIME, UPDATE_TIME) VALUES
('RCM_NEON_B1_1', 'RCP_NEON_BLADE1', 'ITM_NEON_FLUFF', 4, 0, NOW(), NOW()),
('RCM_NEON_B1_2', 'RCP_NEON_BLADE1', 'ITM_NEON_FILM', 2, 1, NOW(), NOW()),
('RCM_NEON_B1_3', 'RCP_NEON_BLADE1', 'ITM_NEON_DUST', 6, 2, NOW(), NOW()),
('RCM_NEON_A1_1', 'RCP_NEON_ARMOR1', 'ITM_NEON_FLUFF', 6, 0, NOW(), NOW()),
('RCM_NEON_A1_2', 'RCP_NEON_ARMOR1', 'ITM_NEON_FILM', 5, 1, NOW(), NOW()),
('RCM_NEON_A1_3', 'RCP_NEON_ARMOR1', 'ITM_NEON_DUST', 3, 2, NOW(), NOW()),
('RCM_NEON_B2_1', 'RCP_NEON_BLADE2', 'ITM_NEON_GRAIN', 6, 0, NOW(), NOW()),
('RCM_NEON_B2_2', 'RCP_NEON_BLADE2', 'ITM_NEON_DUST', 4, 1, NOW(), NOW()),
('RCM_NEON_B2_3', 'RCP_NEON_BLADE2', 'ITM_NEON_FILM', 3, 2, NOW(), NOW()),
('RCM_NEON_A2_1', 'RCP_NEON_ARMOR2', 'ITM_NEON_CORE', 1, 0, NOW(), NOW()),
('RCM_NEON_A2_2', 'RCP_NEON_ARMOR2', 'ITM_NEON_GRAIN', 5, 1, NOW(), NOW()),
('RCM_NEON_A2_3', 'RCP_NEON_ARMOR2', 'ITM_NEON_FLUFF', 4, 2, NOW(), NOW());

-- ============================================================
-- 怪物（一章表格；二章中等威胁略抬）
-- ============================================================
INSERT INTO app_monster (
  id, name, rarity, role_category, grid_h, grid_w, base_atk, base_hp, base_def, base_action, sort,
  normal_skill_id, small_skill_id, ultimate_skill_id, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('MST_NEON_FLUFF', '微光絮虫', 'NORMAL', 'MONSTER', 1, 1, 3, 22, 0, 90, 1001,
 NULL, NULL, NULL, '一章·近战慢', NOW(), NOW()),
('MST_NEON_BUBBLE', '电路软泡', 'NORMAL', 'MONSTER', 1, 1, 2, 16, 0, 120, 1002,
 NULL, NULL, NULL, '一章·慢速', NOW(), NOW()),
('MST_NEON_ANT', '光尘飞蚁', 'NORMAL', 'MONSTER', 1, 1, 2, 12, 0, 60, 1003,
 NULL, NULL, NULL, '一章·攻速最快', NOW(), NOW()),
('MST_NEON_CRYSTAL', '漏电浮晶', 'RARE', 'MONSTER', 1, 2, 10, 80, 2, 76, 1004,
 NULL, 'ASK_NEON_CRY_S', 'ASK_NEON_CRY_U', '二章·中等威胁 占地1×2', NOW(), NOW()),
('MST_NEON_BOSS', '幽光晶核兽', 'BOSS', 'MONSTER', 2, 4, 16, 320, 5, 104, 1005,
 NULL, 'ASK_NEON_BOSS_S', 'ASK_NEON_BOSS_U', '二章BOSS·占地2×4', NOW(), NOW())
ON DUPLICATE KEY UPDATE base_atk=VALUES(base_atk), base_hp=VALUES(base_hp), base_def=VALUES(base_def), base_action=VALUES(base_action),
  rarity=VALUES(rarity), grid_h=VALUES(grid_h), grid_w=VALUES(grid_w), normal_skill_id=VALUES(normal_skill_id), remark=VALUES(remark);

DELETE FROM app_monster_drop WHERE id LIKE 'MDP_NEON_%';
INSERT INTO app_monster_drop (id, monster_id, item_id, drop_rate, min_qty, max_qty, sort, enable, CREATE_TIME, UPDATE_TIME) VALUES
('MDP_NEON_FLUFF', 'MST_NEON_FLUFF', 'ITM_NEON_FLUFF', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_NEON_FILM', 'MST_NEON_BUBBLE', 'ITM_NEON_FILM', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_NEON_DUST', 'MST_NEON_ANT', 'ITM_NEON_DUST', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_NEON_GRAIN', 'MST_NEON_CRYSTAL', 'ITM_NEON_GRAIN', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_NEON_CORE', 'MST_NEON_BOSS', 'ITM_NEON_CORE', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_NEON_BOSS_G', 'MST_NEON_BOSS', 'ITM_NEON_GRAIN', 100, 2, 3, 1, 1, NOW(), NOW());

-- ============================================================
-- 关卡 + 站位（对称）
-- col 0-4, row 0-3（敌方）
-- ============================================================
INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, stamina_cost, remark, CREATE_TIME, UPDATE_TIME) VALUES
('SLV_NEON_01_01', 'SCP_NEON_01', 'LEVEL', '初识絮虫·单体基础试炼', 'NEON1-1', 1, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_01_02', 'SCP_NEON_01', 'LEVEL', '漂浮光球·静态畸变试炼', 'NEON1-2', 2, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_01_03', 'SCP_NEON_01', 'LEVEL', '飞舞光尘·飞行预判试炼', 'NEON1-3', 3, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_01_04', 'SCP_NEON_01', 'LEVEL', '街区混编·多目标试炼', 'NEON1-4', 4, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_01_05', 'SCP_NEON_01', 'LEVEL', '街区终试·集群考核试炼', 'NEON1-5', 5, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_02_01', 'SCP_NEON_02', 'LEVEL', '隘口外围·一阶残留清场', 'NEON2-1', 1, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_02_02', 'SCP_NEON_02', 'LEVEL', '初遇浮晶·进阶畸变试炼', 'NEON2-2', 2, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_02_03', 'SCP_NEON_02', 'LEVEL', '电流集群·浮晶高压试炼', 'NEON2-3', 3, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_02_04', 'SCP_NEON_02', 'LEVEL', '隘口深处·混编突击试炼', 'NEON2-4', 4, 1, 1, NULL, NOW(), NOW()),
('SLV_NEON_02_05', 'SCP_NEON_02', 'LEVEL', '晶核决战·BOSS终极试炼', 'NEON2-5', 5, 1, 1, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), sort=VALUES(sort), enable=1;

DELETE FROM app_stage_level_monster WHERE id LIKE 'SLM_NEON_%';
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
-- Ch1-1: 絮虫×1 中心
('SLM_NEON_0101_1', 'SLV_NEON_01_01', 'MST_NEON_FLUFF', 2, 1, 0, NOW(), NOW()),
-- Ch1-2: 软泡×2
('SLM_NEON_0102_1', 'SLV_NEON_01_02', 'MST_NEON_BUBBLE', 1, 1, 0, NOW(), NOW()),
('SLM_NEON_0102_2', 'SLV_NEON_01_02', 'MST_NEON_BUBBLE', 3, 1, 1, NOW(), NOW()),
-- Ch1-3: 飞蚁×3 后排
('SLM_NEON_0103_1', 'SLV_NEON_01_03', 'MST_NEON_ANT', 1, 0, 0, NOW(), NOW()),
('SLM_NEON_0103_2', 'SLV_NEON_01_03', 'MST_NEON_ANT', 2, 0, 1, NOW(), NOW()),
('SLM_NEON_0103_3', 'SLV_NEON_01_03', 'MST_NEON_ANT', 3, 0, 2, NOW(), NOW()),
-- Ch1-4: 混编 絮1+泡1+蚁2
('SLM_NEON_0104_1', 'SLV_NEON_01_04', 'MST_NEON_FLUFF', 2, 2, 0, NOW(), NOW()),
('SLM_NEON_0104_2', 'SLV_NEON_01_04', 'MST_NEON_BUBBLE', 2, 1, 1, NOW(), NOW()),
('SLM_NEON_0104_3', 'SLV_NEON_01_04', 'MST_NEON_ANT', 1, 0, 2, NOW(), NOW()),
('SLM_NEON_0104_4', 'SLV_NEON_01_04', 'MST_NEON_ANT', 3, 0, 3, NOW(), NOW()),
-- Ch1-5: 絮2+泡2+蚁3
('SLM_NEON_0105_1', 'SLV_NEON_01_05', 'MST_NEON_FLUFF', 1, 2, 0, NOW(), NOW()),
('SLM_NEON_0105_2', 'SLV_NEON_01_05', 'MST_NEON_FLUFF', 3, 2, 1, NOW(), NOW()),
('SLM_NEON_0105_3', 'SLV_NEON_01_05', 'MST_NEON_BUBBLE', 1, 1, 2, NOW(), NOW()),
('SLM_NEON_0105_4', 'SLV_NEON_01_05', 'MST_NEON_BUBBLE', 3, 1, 3, NOW(), NOW()),
('SLM_NEON_0105_5', 'SLV_NEON_01_05', 'MST_NEON_ANT', 1, 0, 4, NOW(), NOW()),
('SLM_NEON_0105_6', 'SLV_NEON_01_05', 'MST_NEON_ANT', 2, 0, 5, NOW(), NOW()),
('SLM_NEON_0105_7', 'SLV_NEON_01_05', 'MST_NEON_ANT', 3, 0, 6, NOW(), NOW()),
-- Ch2-1: 一阶残留对称
('SLM_NEON_0201_1', 'SLV_NEON_02_01', 'MST_NEON_FLUFF', 1, 2, 0, NOW(), NOW()),
('SLM_NEON_0201_2', 'SLV_NEON_02_01', 'MST_NEON_FLUFF', 4, 2, 1, NOW(), NOW()),
('SLM_NEON_0201_3', 'SLV_NEON_02_01', 'MST_NEON_BUBBLE', 1, 1, 2, NOW(), NOW()),
('SLM_NEON_0201_4', 'SLV_NEON_02_01', 'MST_NEON_BUBBLE', 4, 1, 3, NOW(), NOW()),
('SLM_NEON_0201_5', 'SLV_NEON_02_01', 'MST_NEON_ANT', 1, 0, 4, NOW(), NOW()),
('SLM_NEON_0201_6', 'SLV_NEON_02_01', 'MST_NEON_ANT', 4, 0, 5, NOW(), NOW()),
-- Ch2-2: 浮晶×2(各宽2) + 絮×1
('SLM_NEON_0202_1', 'SLV_NEON_02_02', 'MST_NEON_CRYSTAL', 0, 1, 0, NOW(), NOW()),
('SLM_NEON_0202_2', 'SLV_NEON_02_02', 'MST_NEON_CRYSTAL', 4, 1, 1, NOW(), NOW()),
('SLM_NEON_0202_3', 'SLV_NEON_02_02', 'MST_NEON_FLUFF', 2, 2, 2, NOW(), NOW()),
-- Ch2-3: 浮晶×3 对称
('SLM_NEON_0203_1', 'SLV_NEON_02_03', 'MST_NEON_CRYSTAL', 0, 1, 0, NOW(), NOW()),
('SLM_NEON_0203_2', 'SLV_NEON_02_03', 'MST_NEON_CRYSTAL', 2, 2, 1, NOW(), NOW()),
('SLM_NEON_0203_3', 'SLV_NEON_02_03', 'MST_NEON_CRYSTAL', 4, 1, 2, NOW(), NOW()),
-- Ch2-4: 浮晶×2 + 蚁后排
('SLM_NEON_0204_1', 'SLV_NEON_02_04', 'MST_NEON_CRYSTAL', 0, 2, 0, NOW(), NOW()),
('SLM_NEON_0204_2', 'SLV_NEON_02_04', 'MST_NEON_CRYSTAL', 4, 2, 1, NOW(), NOW()),
('SLM_NEON_0204_3', 'SLV_NEON_02_04', 'MST_NEON_ANT', 1, 0, 2, NOW(), NOW()),
('SLM_NEON_0204_4', 'SLV_NEON_02_04', 'MST_NEON_ANT', 2, 0, 3, NOW(), NOW()),
('SLM_NEON_0204_5', 'SLV_NEON_02_04', 'MST_NEON_ANT', 3, 0, 4, NOW(), NOW()),
('SLM_NEON_0204_6', 'SLV_NEON_02_04', 'MST_NEON_ANT', 4, 0, 5, NOW(), NOW()),
-- Ch2-5: BOSS 2×4 居中 (col1 row1)
('SLM_NEON_0205_1', 'SLV_NEON_02_05', 'MST_NEON_BOSS', 1, 1, 0, NOW(), NOW());

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'chapters' AS t, id, name, code FROM app_stage WHERE id LIKE 'SCP_NEON_%';
SELECT 'levels' AS t, COUNT(*) c FROM app_stage WHERE id LIKE 'SLV_NEON_%';
SELECT 'recipes' AS t, id, unlock_chapter_id FROM app_recipe WHERE id LIKE 'RCP_NEON_%';
SELECT 'monsters' AS t, id, base_hp, base_atk, base_action FROM app_monster WHERE id LIKE 'MST_NEON_%';
