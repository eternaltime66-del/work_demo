-- ============================================================
-- 第一章「蒸汽与齿轮」内容种子（引擎可配版）
-- 来源：src/main/resources/千问的关卡数据/第一章.md
-- 全部 ID 前缀 *_CH1_* ；可与主线种子并存
-- 若已插入过：先执行 rollback_ch1_steam.sql 再跑本文件
-- 依赖：STY_10000001（主线类型）、DEFAULT_NORMAL 已存在
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- 公式片段
SET @F_ATK_05  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"0.5"}]';
SET @F_ATK_08  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"0.8"}]';
SET @F_ATK_1   = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1"}]';
SET @F_ATK_12  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1.2"}]';
SET @F_ATK_15  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"1.5"}]';
SET @F_ATK_2   = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"2"}]';
SET @F_ATK_25  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"2.5"}]';
SET @F_ATK_02  = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"ATK"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"0.2"}]';
SET @F_HP_01   = '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readCategory":"ATTR","readKey":"MAX_HP"},{"kind":"OP","op":"*"},{"kind":"PARAM","paramMode":"LITERAL","value":"0.1"}]';
SET @F_LIT_4   = '[{"kind":"PARAM","paramMode":"LITERAL","value":"4"}]';
SET @F_LIT_5   = '[{"kind":"PARAM","paramMode":"LITERAL","value":"5"}]';
SET @F_LIT_15  = '[{"kind":"PARAM","paramMode":"LITERAL","value":"15"}]';
SET @F_LIT_30  = '[{"kind":"PARAM","paramMode":"LITERAL","value":"30"}]';
SET @F_LIT_50  = '[{"kind":"PARAM","paramMode":"LITERAL","value":"50"}]';

-- ============================================================
-- 1) BuffDef
-- ============================================================
INSERT INTO app_buff_def (
  id, name, code, buff_kind, beneficial, dispelable, stack_mode, max_stacks, duration_av,
  attr_key, attr_dir, formula_json,
  pulse_every_av, pulse_effect_type, damage_element, pulse_target_type,
  sort, enable, remark, create_time, update_time
) VALUES
('BFD_CH1_ATKUP', '攻击强化', 'CH1_ATKUP', 'ATTR', 1, 1, 'NONE', 0, 150,
 'FINAL_ATK', 'INCREASE', @F_LIT_30,
 NULL, NULL, 'PHYSICAL', NULL,
 0, 1, '蒸汽充能核心·群体过载', NOW(), NOW()),
('BFD_CH1_BURN', '灼烧', 'CH1_BURN', 'PULSE', 0, 1, 'NONE', 0, 200,
 NULL, NULL, @F_LIT_5,
 50, 'DAMAGE', 'BURN', 'SELF',
 1, 1, '超压喷射·脉冲灼烧', NOW(), NOW()),
('BFD_CH1_DRAIN', '能量流失', 'CH1_DRAIN', 'ATTR', 0, 1, 'NONE', 0, 50,
 'ATK_SPEED', 'DECREASE', @F_LIT_15,
 NULL, NULL, 'PHYSICAL', NULL,
 2, 1, '虹吸斩·降攻速', NOW(), NOW());

-- ============================================================
-- 2) 主动技能（怪物）
-- ============================================================
INSERT INTO app_active_skill (
  id, name, skill_type, skill_school, damage_element, code,
  need_charge_mode, need_charge, max_cast_skill, max_cast_global, max_cast_all_means, max_cast_role,
  sort, enable, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('ASK_CH1_GEAR_N', '钝器敲击', 'NORMAL', '无', 'PHYSICAL', 'CH1_GEAR_N',
 'SELF_BASE_ACTION', 0, 0, 0, 0, 0, 1, 1, '锈蚀齿轮兵普攻', NOW(), NOW()),
('ASK_CH1_GEAR_S', '疯狂连打', 'SMALL', '无', 'PHYSICAL', 'CH1_GEAR_S',
 'MANUAL', 20, 0, 0, 0, 0, 2, 1, '锈蚀齿轮兵小技能', NOW(), NOW()),
('ASK_CH1_CORE_N', '电流溢散', 'NORMAL', '无', 'SHOCK', 'CH1_CORE_N',
 'SELF_BASE_ACTION', 0, 0, 0, 0, 0, 3, 1, '蒸汽充能核心普攻', NOW(), NOW()),
('ASK_CH1_CORE_S', '群体过载', 'SMALL', '无', 'PHYSICAL', 'CH1_CORE_S',
 'MANUAL', 30, 0, 0, 0, 0, 4, 1, '蒸汽充能核心小技能', NOW(), NOW()),
('ASK_CH1_FURN_N', '熔岩重击', 'NORMAL', '无', 'PHYSICAL', 'CH1_FURN_N',
 'SELF_BASE_ACTION', 0, 0, 0, 0, 0, 5, 1, '暴走熔炉核心普攻', NOW(), NOW()),
('ASK_CH1_FURN_S', '紧急冷却', 'SMALL', '无', 'PHYSICAL', 'CH1_FURN_S',
 'MANUAL', 20, 0, 0, 0, 0, 6, 1, '暴走熔炉核心小技能', NOW(), NOW()),
('ASK_CH1_BOSS_N', '蒸汽重拳', 'NORMAL', '无', 'PHYSICAL', 'CH1_BOSS_N',
 'SELF_BASE_ACTION', 0, 0, 0, 0, 0, 7, 1, '巨型蒸汽傀儡普攻', NOW(), NOW()),
('ASK_CH1_BOSS_S', '齿轮风暴', 'SMALL', '无', 'PHYSICAL', 'CH1_BOSS_S',
 'MANUAL', 100, 0, 0, 0, 0, 8, 1, '巨型蒸汽傀儡小技能(原召唤替换)', NOW(), NOW()),
('ASK_CH1_BOSS_U', '超压喷射', 'ULTIMATE', '无', 'BURN', 'CH1_BOSS_U',
 'MANUAL', 100, 0, 0, 0, 0, 9, 1, '巨型蒸汽傀儡大招', NOW(), NOW());

-- 玩家武器技能
INSERT INTO app_active_skill (
  id, name, skill_type, skill_school, damage_element, code,
  need_charge_mode, need_charge, max_cast_skill, max_cast_global, max_cast_all_means, max_cast_role,
  sort, enable, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('ASK_CH1_SIPHON_N', '虹吸斩', 'NORMAL', '虹吸', 'PHYSICAL', 'CH1_SIPHON_N',
 'SELF_BASE_ACTION', 0, 0, 0, 0, 0, 10, 1, '虹吸·动力刺剑普攻', NOW(), NOW()),
('ASK_CH1_OVERLOAD', '超载释放', 'SMALL', '虹吸', 'PHYSICAL', 'CH1_OVERLOAD',
 'MANUAL', 100, 0, 0, 0, 0, 11, 1, '虹吸·动力刺剑充能技', NOW(), NOW());

-- 充能条件
INSERT INTO app_skill_charge (
  id, skill_id, name, condition_type, scope, every_action_value, charge_gain,
  skill_charge_event, skill_charge_match, match_skill_type, match_skill_id, sort, CREATE_TIME, UPDATE_TIME
) VALUES
('SCH_CH1_GEAR_S1', 'ASK_CH1_GEAR_S', '行动值充能', 'ACTION_VALUE', 'GLOBAL', 80, 20,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_CORE_S1', 'ASK_CH1_CORE_S', '行动值充能', 'ACTION_VALUE', 'GLOBAL', 150, 30,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_FURN_S1', 'ASK_CH1_FURN_S', '受伤充能', 'TAKE_ANY_DAMAGE', 'GLOBAL', NULL, 4,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_BOSS_S1', 'ASK_CH1_BOSS_S', '行动值充能', 'ACTION_VALUE', 'GLOBAL', 400, 100,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_BOSS_U1', 'ASK_CH1_BOSS_U', '行动值充能', 'ACTION_VALUE', 'GLOBAL', 800, 100,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_OL_1', 'ASK_CH1_OVERLOAD', '造成伤害充能', 'DEAL_ANY_DAMAGE', 'GLOBAL', NULL, 5,
 NULL, NULL, NULL, NULL, 0, NOW(), NOW()),
('SCH_CH1_OL_2', 'ASK_CH1_OVERLOAD', '释放普攻充能', 'SKILL_CHARGE', 'GLOBAL', NULL, 10,
 'CAST', 'ANY_TYPE', 'NORMAL', NULL, 1, NOW(), NOW()),
('SCH_CH1_OL_3', 'ASK_CH1_OVERLOAD', '虹吸斩充能', 'SKILL_CHARGE', 'GLOBAL', NULL, 15,
 'CAST', 'SPECIFIC', NULL, 'ASK_CH1_SIPHON_N', 2, NOW(), NOW());

-- 输出（主动）
INSERT INTO app_skill_output (
  id, skill_id, passive_skill_id, name, output_kind, target_type,
  attr_key, attr_dir, effect_type, damage_element, formula_json,
  hit_segments, trigger_rate, duration_av, buff_def_id, sort, create_time, update_time
) VALUES
('SOUT_CH1_GEAR_N', 'ASK_CH1_GEAR_N', NULL, '钝器敲击', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_1, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_GEAR_S', 'ASK_CH1_GEAR_S', NULL, '疯狂连打', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_15, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_CORE_N', 'ASK_CH1_CORE_N', NULL, '电流溢散', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'SHOCK', @F_ATK_05, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_CORE_S', 'ASK_CH1_CORE_S', NULL, '群体过载', 'APPEND_BUFF', 'ALL_ALLY',
 NULL, NULL, NULL, 'PHYSICAL', NULL, 1, 100, 0, 'BFD_CH1_ATKUP', 0, NOW(), NOW()),
('SOUT_CH1_FURN_N', 'ASK_CH1_FURN_N', NULL, '熔岩重击', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_12, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_FURN_S', 'ASK_CH1_FURN_S', NULL, '紧急冷却', 'EFFECT', 'SELF',
 NULL, NULL, 'HEAL', 'PHYSICAL', @F_HP_01, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_BOSS_N', 'ASK_CH1_BOSS_N', NULL, '蒸汽重拳', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_12, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_BOSS_S1', 'ASK_CH1_BOSS_S', NULL, '齿轮风暴伤害', 'EFFECT', 'ALL_ENEMY',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_08, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_BOSS_S2', 'ASK_CH1_BOSS_S', NULL, '齿轮风暴强化', 'ATTR', 'SELF',
 'ATK', 'INCREASE', NULL, 'PHYSICAL', @F_LIT_4, 1, 100, 400, NULL, 1, NOW(), NOW()),
('SOUT_CH1_BOSS_U1', 'ASK_CH1_BOSS_U', NULL, '超压喷射伤害', 'EFFECT', 'ALL_ENEMY',
 NULL, NULL, 'DAMAGE', 'BURN', @F_ATK_2, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_BOSS_U2', 'ASK_CH1_BOSS_U', NULL, '超压喷射灼烧', 'APPEND_BUFF', 'ALL_ENEMY',
 NULL, NULL, NULL, 'BURN', NULL, 1, 100, 0, 'BFD_CH1_BURN', 1, NOW(), NOW()),
('SOUT_CH1_SIPH_N1', 'ASK_CH1_SIPHON_N', NULL, '虹吸斩伤害', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_1, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_SIPH_N2', 'ASK_CH1_SIPHON_N', NULL, '能量流失', 'APPEND_BUFF', 'FIRST',
 NULL, NULL, NULL, 'PHYSICAL', NULL, 1, 100, 0, 'BFD_CH1_DRAIN', 1, NOW(), NOW()),
('SOUT_CH1_OL_1', 'ASK_CH1_OVERLOAD', NULL, '超载伤害', 'EFFECT', 'FIRST',
 NULL, NULL, 'DAMAGE', 'PHYSICAL', @F_ATK_25, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_OL_2', 'ASK_CH1_OVERLOAD', NULL, '超频攻速', 'ATTR', 'SELF',
 'ATK_SPEED', 'INCREASE', NULL, 'PHYSICAL', @F_LIT_50, 1, 100, 150, NULL, 1, NOW(), NOW());

-- ============================================================
-- 3) 被动
-- ============================================================
INSERT INTO app_passive_skill (
  id, name, code, passive_type, condition_mode,
  skill_match_mode, ref_skill_type, ref_skill_id,
  combat_event, sort, enable, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('PSK_CH1_COUPLE', '能量耦合', 'CH1_COUPLE', 'BATTLE_COMBAT', 'UNLIMITED',
 'SPECIFIC', NULL, 'ASK_CH1_SIPHON_N',
 'AFTER_DEAL_ACTIVE_DMG', 0, 1, '虹吸斩造成主动伤后追加电击+治疗', NOW(), NOW()),
('PSK_CH1_SPD', '动能加速', 'CH1_SPD', 'OUT_ADVANCED', 'UNLIMITED',
 NULL, NULL, NULL,
 NULL, 1, 1, '刺剑攻速+10%', NOW(), NOW());

INSERT INTO app_passive_effect (id, skill_id, attr_key, attr_dir, value_num, sort, CREATE_TIME, UPDATE_TIME) VALUES
('PSE_CH1_SPD1', 'PSK_CH1_SPD', 'ATK_SPEED', 'INCREASE', 10, 0, NOW(), NOW());

INSERT INTO app_skill_output (
  id, skill_id, passive_skill_id, name, output_kind, target_type,
  attr_key, attr_dir, effect_type, damage_element, formula_json,
  hit_segments, trigger_rate, duration_av, buff_def_id, sort, create_time, update_time
) VALUES
('SOUT_CH1_CPL_D', NULL, 'PSK_CH1_COUPLE', '耦合电击', 'EFFECT', 'EVENT_HIT_TARGETS',
 NULL, NULL, 'DAMAGE', 'SHOCK', @F_ATK_05, 1, 100, 0, NULL, 0, NOW(), NOW()),
('SOUT_CH1_CPL_H', NULL, 'PSK_CH1_COUPLE', '耦合吸血', 'EFFECT', 'SELF',
 NULL, NULL, 'HEAL', 'PHYSICAL', @F_ATK_02, 1, 100, 0, NULL, 1, NOW(), NOW());

-- ============================================================
-- 4) 物品：材料 + 武器
-- ============================================================
INSERT INTO app_item (
  id, code, name, icon, item_type, max_stack, sort, enable, remark,
  charge_skill_slot_count, player_default_edit_charge_skill_slot_count, player_max_edit_charge_skill_slot_count,
  basic_passive_slot_count, advanced_passive_slot_count, battle_combat_passive_slot_count,
  CREATE_TIME, UPDATE_TIME
) VALUES
('ITM_CH1_GEAR', 'ch1_broken_gear', '破损的齿轮', NULL, 'MATERIAL', 99, 9001, 1, '第一章材料',
 0, 0, 0, 0, 0, 0, NOW(), NOW()),
('ITM_CH1_CORE', 'ch1_energy_core', '稳定的能量核心', NULL, 'MATERIAL', 99, 9002, 1, '第一章材料',
 0, 0, 0, 0, 0, 0, NOW(), NOW()),
('ITM_CH1_FURN', 'ch1_furnace_core', '过热的熔炉核心', NULL, 'MATERIAL', 99, 9003, 1, '第一章材料',
 0, 0, 0, 0, 0, 0, NOW(), NOW()),
('ITM_CH1_PISTON', 'ch1_giant_piston', '巨型活塞', NULL, 'MATERIAL', 99, 9004, 1, '第一章材料',
 0, 0, 0, 0, 0, 0, NOW(), NOW()),
('ITM_CH1_WOOD', 'ch1_wood_sword', '初始木剑', NULL, 'WEAPON', 1, 9101, 1, '第一章初始武器',
 0, 0, 0, 0, 0, 0, NOW(), NOW()),
('ITM_CH1_SIPHON', 'ch1_siphon_sword', '虹吸·动力刺剑', NULL, 'WEAPON', 1, 9102, 1, '第一章风味武器',
 1, 0, 0, 0, 1, 1, NOW(), NOW());

INSERT INTO app_item_material (id, item_id, grade, CREATE_TIME, UPDATE_TIME) VALUES
('MAT_CH1_GEAR', 'ITM_CH1_GEAR', 1, NOW(), NOW()),
('MAT_CH1_CORE', 'ITM_CH1_CORE', 1, NOW(), NOW()),
('MAT_CH1_FURN', 'ITM_CH1_FURN', 2, NOW(), NOW()),
('MAT_CH1_PISTON', 'ITM_CH1_PISTON', 3, NOW(), NOW());

INSERT INTO app_item_weapon (id, item_id, base_atk, atk_speed_up_ratio, atk_speed_down_ratio, normal_skill_id, remark, CREATE_TIME, UPDATE_TIME) VALUES
('WPN_CH1_WOOD', 'ITM_CH1_WOOD', 2, 0, 0, NULL, '使用系统默认普攻', NOW(), NOW()),
('WPN_CH1_SIPHON', 'ITM_CH1_SIPHON', 5, 0, 0, 'ASK_CH1_SIPHON_N', '攻速由 OUT 被动动能加速提供', NOW(), NOW());

INSERT INTO app_item_default_skill (id, item_id, skill_id, slot_no, sort, CREATE_TIME, UPDATE_TIME) VALUES
('IDS_CH1_SIPHON_1', 'ITM_CH1_SIPHON', 'ASK_CH1_OVERLOAD', 0, 0, NOW(), NOW());

INSERT INTO app_item_default_passive (id, item_id, passive_skill_id, passive_type, slot_no, sort, CREATE_TIME, UPDATE_TIME) VALUES
('IDP_CH1_SIPHON_1', 'ITM_CH1_SIPHON', 'PSK_CH1_SPD', 'OUT_ADVANCED', 0, 0, NOW(), NOW()),
('IDP_CH1_SIPHON_2', 'ITM_CH1_SIPHON', 'PSK_CH1_COUPLE', 'BATTLE_COMBAT', 0, 1, NOW(), NOW());

-- ============================================================
-- 5) 配方
-- ============================================================
INSERT INTO app_recipe (id, name, output_item_id, output_qty, sort, enable, remark, CREATE_TIME, UPDATE_TIME) VALUES
('RCP_CH1_SIPHON', '虹吸·动力刺剑', 'ITM_CH1_SIPHON', 1, 9001, 1, '核心×2 + 齿轮×5', NOW(), NOW());

INSERT INTO app_recipe_material (id, recipe_id, item_id, quantity, sort, CREATE_TIME, UPDATE_TIME) VALUES
('RCM_CH1_SIPHON_1', 'RCP_CH1_SIPHON', 'ITM_CH1_CORE', 2, 0, NOW(), NOW()),
('RCM_CH1_SIPHON_2', 'RCP_CH1_SIPHON', 'ITM_CH1_GEAR', 5, 1, NOW(), NOW());

-- ============================================================
-- 6) 怪物 + 掉落
-- ============================================================
INSERT INTO app_monster (
  id, name, rarity, grid_h, grid_w, base_atk, base_hp, base_def, base_action, sort,
  normal_skill_id, small_skill_id, ultimate_skill_id, remark, CREATE_TIME, UPDATE_TIME
) VALUES
('MST_CH1_GEAR', '锈蚀齿轮兵', 'NORMAL', 1, 1, 8, 40, 0, 80, 9001,
 'ASK_CH1_GEAR_N', 'ASK_CH1_GEAR_S', NULL, '第一章炮灰', NOW(), NOW()),
('MST_CH1_CORE', '蒸汽充能核心', 'RARE', 1, 2, 0, 120, 5, 120, 9002,
 'ASK_CH1_CORE_N', 'ASK_CH1_CORE_S', NULL, '第一章BUFF机', NOW(), NOW()),
('MST_CH1_FURN', '暴走熔炉核心', 'RARE', 1, 2, 15, 300, 8, 100, 9003,
 'ASK_CH1_FURN_N', 'ASK_CH1_FURN_S', NULL, '第一章精英', NOW(), NOW()),
('MST_CH1_BOSS', '巨型蒸汽傀儡', 'EPIC', 2, 2, 20, 800, 10, 150, 9004,
 'ASK_CH1_BOSS_N', 'ASK_CH1_BOSS_S', 'ASK_CH1_BOSS_U', '第一章Boss(占地EPIC 2x2)', NOW(), NOW());

INSERT INTO app_monster_drop (id, monster_id, item_id, drop_rate, min_qty, max_qty, sort, enable, CREATE_TIME, UPDATE_TIME) VALUES
('MDP_CH1_GEAR_1', 'MST_CH1_GEAR', 'ITM_CH1_GEAR', 80, 1, 2, 0, 1, NOW(), NOW()),
('MDP_CH1_CORE_1', 'MST_CH1_CORE', 'ITM_CH1_CORE', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_CH1_FURN_1', 'MST_CH1_FURN', 'ITM_CH1_FURN', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_CH1_FURN_2', 'MST_CH1_FURN', 'ITM_CH1_GEAR', 50, 2, 4, 1, 1, NOW(), NOW()),
('MDP_CH1_BOSS_1', 'MST_CH1_BOSS', 'ITM_CH1_PISTON', 100, 1, 1, 0, 1, NOW(), NOW()),
('MDP_CH1_BOSS_2', 'MST_CH1_BOSS', 'ITM_CH1_CORE', 50, 1, 1, 1, 1, NOW(), NOW());

-- ============================================================
-- 7) 关卡（挂主线类型 STY_10000001）
-- 坐标：引擎 0-based；方案文档 1-based → 已减 1
-- ============================================================
INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME) VALUES
('SCP_CH1', 'STY_10000001', 'CHAPTER', '蒸汽与齿轮', 'CH1', 80, 1, '千问第一章·引擎可配版', NOW(), NOW());

INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME) VALUES
('SLV_CH1_01', 'SCP_CH1', 'LEVEL', '齿轮初转', 'CH1-1', 1, 1, '教学热身', NOW(), NOW()),
('SLV_CH1_02', 'SCP_CH1', 'LEVEL', '能量过载', 'CH1-2', 2, 1, '引入BUFF机', NOW(), NOW()),
('SLV_CH1_03', 'SCP_CH1', 'LEVEL', '交叉火力', 'CH1-3', 3, 1, '双核心', NOW(), NOW()),
('SLV_CH1_04', 'SCP_CH1', 'LEVEL', '熔炉暴走', 'CH1-4', 4, 1, '精英拉锯', NOW(), NOW()),
('SLV_CH1_05', 'SCP_CH1', 'LEVEL', '蒸汽王座', 'CH1-5', 5, 1, '章节Boss', NOW(), NOW());

-- 1-1
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
('SLM_CH1_01_A', 'SLV_CH1_01', 'MST_CH1_GEAR', 0, 0, 1, NOW(), NOW()),
('SLM_CH1_01_B', 'SLV_CH1_01', 'MST_CH1_GEAR', 2, 0, 2, NOW(), NOW()),
('SLM_CH1_01_C', 'SLV_CH1_01', 'MST_CH1_GEAR', 4, 0, 3, NOW(), NOW()),
('SLM_CH1_01_D', 'SLV_CH1_01', 'MST_CH1_GEAR', 2, 1, 4, NOW(), NOW());

-- 1-2
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
('SLM_CH1_02_A', 'SLV_CH1_02', 'MST_CH1_GEAR', 0, 0, 1, NOW(), NOW()),
('SLM_CH1_02_B', 'SLV_CH1_02', 'MST_CH1_GEAR', 2, 0, 2, NOW(), NOW()),
('SLM_CH1_02_C', 'SLV_CH1_02', 'MST_CH1_GEAR', 4, 0, 3, NOW(), NOW()),
('SLM_CH1_02_D', 'SLV_CH1_02', 'MST_CH1_CORE', 1, 1, 4, NOW(), NOW());

-- 1-3
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
('SLM_CH1_03_A', 'SLV_CH1_03', 'MST_CH1_GEAR', 0, 0, 1, NOW(), NOW()),
('SLM_CH1_03_B', 'SLV_CH1_03', 'MST_CH1_GEAR', 1, 0, 2, NOW(), NOW()),
('SLM_CH1_03_C', 'SLV_CH1_03', 'MST_CH1_GEAR', 3, 0, 3, NOW(), NOW()),
('SLM_CH1_03_D', 'SLV_CH1_03', 'MST_CH1_GEAR', 4, 0, 4, NOW(), NOW()),
('SLM_CH1_03_E', 'SLV_CH1_03', 'MST_CH1_CORE', 0, 1, 5, NOW(), NOW()),
('SLM_CH1_03_F', 'SLV_CH1_03', 'MST_CH1_CORE', 3, 1, 6, NOW(), NOW());

-- 1-4
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
('SLM_CH1_04_A', 'SLV_CH1_04', 'MST_CH1_GEAR', 1, 0, 1, NOW(), NOW()),
('SLM_CH1_04_B', 'SLV_CH1_04', 'MST_CH1_GEAR', 3, 0, 2, NOW(), NOW()),
('SLM_CH1_04_C', 'SLV_CH1_04', 'MST_CH1_FURN', 1, 1, 3, NOW(), NOW());

-- 1-5
INSERT INTO app_stage_level_monster (id, level_id, monster_id, pos_col, pos_row, sort, CREATE_TIME, UPDATE_TIME) VALUES
('SLM_CH1_05_A', 'SLV_CH1_05', 'MST_CH1_GEAR', 0, 0, 1, NOW(), NOW()),
('SLM_CH1_05_B', 'SLV_CH1_05', 'MST_CH1_GEAR', 4, 0, 2, NOW(), NOW()),
('SLM_CH1_05_C', 'SLV_CH1_05', 'MST_CH1_BOSS', 1, 1, 3, NOW(), NOW());

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- 完成：章节 SCP_CH1 / 关卡 CH1-1~5 / 怪·技能·装备·配方 已写入
