-- 技能 V2：统一输出 / BUFF / 被动锚点字段 / 主动充能公式
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_buff_def (
  id varchar(32) NOT NULL PRIMARY KEY,
  name varchar(128) DEFAULT NULL,
  code varchar(64) DEFAULT NULL,
  buff_kind varchar(32) DEFAULT NULL,
  beneficial tinyint(1) DEFAULT 1,
  dispelable tinyint(1) DEFAULT 1,
  stack_mode varchar(16) DEFAULT 'NONE',
  max_stacks int DEFAULT 0,
  duration_av int DEFAULT 0,
  attr_key varchar(32) DEFAULT NULL,
  attr_dir varchar(16) DEFAULT NULL,
  formula_json text,
  pulse_every_av int DEFAULT 0,
  pulse_effect_type varchar(16) DEFAULT NULL,
  damage_element varchar(16) DEFAULT 'PHYSICAL',
  pulse_target_type varchar(32) DEFAULT NULL,
  left_formula_json text,
  compare_op varchar(8) DEFAULT NULL,
  right_formula_json text,
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  more text,
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_skill_output (
  id varchar(32) NOT NULL PRIMARY KEY,
  skill_id varchar(32) DEFAULT NULL,
  passive_skill_id varchar(32) DEFAULT NULL,
  name varchar(128) DEFAULT NULL,
  output_kind varchar(16) DEFAULT NULL,
  target_type varchar(32) DEFAULT NULL,
  attr_key varchar(32) DEFAULT NULL,
  attr_dir varchar(16) DEFAULT NULL,
  effect_type varchar(16) DEFAULT NULL,
  damage_element varchar(16) DEFAULT 'PHYSICAL',
  formula_json text,
  hit_segments int DEFAULT 1,
  trigger_rate int DEFAULT 100,
  duration_av int DEFAULT 0,
  buff_def_id varchar(32) DEFAULT NULL,
  sort int DEFAULT 0,
  remark varchar(255) DEFAULT NULL,
  more text,
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  KEY idx_sout_skill (skill_id),
  KEY idx_sout_passive (passive_skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE app_passive_skill
  ADD COLUMN combat_event varchar(48) DEFAULT NULL COMMENT 'BATTLE_COMBAT事件' AFTER max_trigger_per_battle,
  ADD COLUMN start_apply_rule varchar(32) DEFAULT NULL COMMENT '开战施加规则' AFTER combat_event,
  ADD COLUMN start_elapsed_av int DEFAULT 0 COMMENT '开战时间规则行动值' AFTER start_apply_rule;

ALTER TABLE app_active_skill
  ADD COLUMN need_charge_formula_json text COMMENT '充能阈值公式' AFTER need_charge;

ALTER TABLE app_item
  ADD COLUMN battle_start_passive_slot_count int DEFAULT 0 AFTER sustained_passive_slot_count,
  ADD COLUMN player_default_edit_battle_start_passive_slot_count int DEFAULT 0 AFTER battle_start_passive_slot_count,
  ADD COLUMN player_max_edit_battle_start_passive_slot_count int DEFAULT 0 AFTER player_default_edit_battle_start_passive_slot_count,
  ADD COLUMN battle_judge_passive_slot_count int DEFAULT 0 AFTER player_max_edit_battle_start_passive_slot_count,
  ADD COLUMN player_default_edit_battle_judge_passive_slot_count int DEFAULT 0 AFTER battle_judge_passive_slot_count,
  ADD COLUMN player_max_edit_battle_judge_passive_slot_count int DEFAULT 0 AFTER player_default_edit_battle_judge_passive_slot_count,
  ADD COLUMN battle_pulse_passive_slot_count int DEFAULT 0 AFTER player_max_edit_battle_judge_passive_slot_count,
  ADD COLUMN player_default_edit_battle_pulse_passive_slot_count int DEFAULT 0 AFTER battle_pulse_passive_slot_count,
  ADD COLUMN player_max_edit_battle_pulse_passive_slot_count int DEFAULT 0 AFTER player_default_edit_battle_pulse_passive_slot_count,
  ADD COLUMN battle_combat_passive_slot_count int DEFAULT 0 AFTER player_max_edit_battle_pulse_passive_slot_count,
  ADD COLUMN player_default_edit_battle_combat_passive_slot_count int DEFAULT 0 AFTER battle_combat_passive_slot_count,
  ADD COLUMN player_max_edit_battle_combat_passive_slot_count int DEFAULT 0 AFTER player_default_edit_battle_combat_passive_slot_count;

-- 旧战斗被动禁用（推倒重来）
UPDATE app_passive_skill SET enable = 0
WHERE passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED') AND (enable IS NULL OR enable = 1);
