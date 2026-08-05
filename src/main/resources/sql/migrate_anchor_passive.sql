-- 锚点被动：技能锚点字段 + 战斗效果表 + 装备锚点被动槽
SET NAMES utf8mb4;

ALTER TABLE app_passive_skill
  ADD COLUMN anchor_type varchar(32) DEFAULT NULL COMMENT 'IN_ANCHOR: AFTER_CAST_CHARGE/AFTER_RECEIVE_CHARGE/AFTER_TAKE_CHARGE_DMG/AFTER_DEAL_CHARGE_DMG' AFTER condition_mode,
  ADD COLUMN skill_match_mode varchar(32) DEFAULT NULL COMMENT 'ANY/ANY_TYPE/SPECIFIC' AFTER anchor_type,
  ADD COLUMN ref_skill_type varchar(32) DEFAULT NULL COMMENT 'ActiveSkillType' AFTER skill_match_mode,
  ADD COLUMN ref_skill_id varchar(64) DEFAULT NULL COMMENT '指定技能id' AFTER ref_skill_type;

CREATE TABLE IF NOT EXISTS app_passive_combat_effect (
  id varchar(64) NOT NULL,
  skill_id varchar(64) NOT NULL COMMENT '被动技能id',
  name varchar(255) DEFAULT NULL COMMENT '效果名',
  target_type varchar(32) DEFAULT NULL COMMENT 'SkillEffectTarget',
  effect_type varchar(32) DEFAULT NULL COMMENT 'DAMAGE/HEAL/ATTR_MODIFY',
  attr_key varchar(32) DEFAULT NULL COMMENT 'ATTR_MODIFY: AttrModifyKey',
  attr_dir varchar(32) DEFAULT NULL COMMENT 'INCREASE/DECREASE',
  formula_json longtext COMMENT '公式 token JSON',
  hit_segments int DEFAULT 1 COMMENT '段数',
  sort int DEFAULT 0,
  remark varchar(255) DEFAULT NULL,
  more longtext,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_pce_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战斗内被动效果（锚点等）';

ALTER TABLE app_item
  ADD COLUMN anchor_passive_slot_count int DEFAULT 0 COMMENT '默认自带锚点被动数量' AFTER player_max_edit_advanced_passive_slot_count,
  ADD COLUMN player_default_edit_anchor_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑锚点被动数量' AFTER anchor_passive_slot_count,
  ADD COLUMN player_max_edit_anchor_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大锚点被动数量' AFTER player_default_edit_anchor_passive_slot_count;
