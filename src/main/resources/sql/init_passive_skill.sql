-- 技能 V2：被动技能与生效条件
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_passive_skill (
  id varchar(64) NOT NULL,
  name varchar(255) DEFAULT NULL COMMENT '被动名称',
  code varchar(64) DEFAULT NULL COMMENT '编码',
  passive_type varchar(32) NOT NULL COMMENT 'OUT_BASIC/OUT_ADVANCED/BATTLE_START/BATTLE_JUDGE/BATTLE_PULSE/BATTLE_COMBAT',
  condition_mode varchar(32) DEFAULT 'UNLIMITED' COMMENT 'UNLIMITED/SELECT',
  skill_match_mode varchar(32) DEFAULT NULL COMMENT 'ANY/ANY_TYPE/ANY_SCHOOL/ANY_ELEMENT/SPECIFIC',
  ref_skill_type varchar(32) DEFAULT NULL,
  ref_skill_school varchar(64) DEFAULT NULL,
  ref_damage_element varchar(32) DEFAULT NULL,
  ref_skill_id varchar(64) DEFAULT NULL,
  left_formula_json longtext,
  compare_op varchar(16) DEFAULT NULL,
  right_formula_json longtext,
  max_trigger_per_battle int DEFAULT NULL,
  combat_event varchar(48) DEFAULT NULL,
  start_apply_rule varchar(32) DEFAULT NULL,
  start_elapsed_av int DEFAULT 0,
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  more longtext,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_passive_type (passive_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被动技能';

CREATE TABLE IF NOT EXISTS app_passive_condition (
  id varchar(64) NOT NULL,
  skill_id varchar(64) NOT NULL COMMENT '被动技能 ID',
  condition_type varchar(32) NOT NULL COMMENT 'EQUIP_ITEM/EQUIP_ITEM_TYPE/EQUIP_SKILL/EQUIP_SKILL_TYPE/FORMULA_COMPARE',
  ref_item_id varchar(64) DEFAULT NULL,
  ref_item_type varchar(32) DEFAULT NULL,
  ref_skill_id varchar(64) DEFAULT NULL,
  ref_skill_type varchar(32) DEFAULT NULL,
  left_formula_json longtext,
  compare_op varchar(16) DEFAULT NULL,
  right_formula_json longtext,
  sort int DEFAULT 0,
  remark varchar(255) DEFAULT NULL,
  more longtext,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_passive_cond_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被动技能生效条件';
