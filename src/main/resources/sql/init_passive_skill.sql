-- 被动技能（战斗外基础/高级属性等）+ 生效条件
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_passive_skill (
  id varchar(64) NOT NULL,
  name varchar(255) DEFAULT NULL COMMENT '被动名称',
  code varchar(64) DEFAULT NULL COMMENT '编码',
  passive_type varchar(32) NOT NULL COMMENT 'OUT_BASIC/OUT_ADVANCED/IN_ANCHOR/IN_PERIODIC',
  condition_mode varchar(32) DEFAULT 'UNLIMITED' COMMENT 'UNLIMITED不限 / SELECT选择条件',
  anchor_type varchar(32) DEFAULT NULL COMMENT 'IN_ANCHOR 锚点',
  skill_match_mode varchar(32) DEFAULT NULL COMMENT 'ANY/ANY_TYPE/SPECIFIC',
  ref_skill_type varchar(32) DEFAULT NULL,
  ref_skill_id varchar(64) DEFAULT NULL,
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  more longtext COMMENT '扩展JSON',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_passive_type (passive_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被动技能';

CREATE TABLE IF NOT EXISTS app_passive_condition (
  id varchar(64) NOT NULL,
  skill_id varchar(64) NOT NULL COMMENT '被动技能id',
  condition_type varchar(32) NOT NULL COMMENT 'EQUIP_ITEM/EQUIP_ITEM_TYPE/EQUIP_SKILL/EQUIP_SKILL_TYPE/FORMULA_COMPARE',
  ref_item_id varchar(64) DEFAULT NULL COMMENT '指定装备物品id',
  ref_item_type varchar(32) DEFAULT NULL COMMENT '指定装备类型 ItemType',
  ref_skill_id varchar(64) DEFAULT NULL COMMENT '指定技能id',
  ref_skill_type varchar(32) DEFAULT NULL COMMENT '指定技能类型 ActiveSkillType',
  left_formula_json longtext COMMENT '公式判定-左公式',
  compare_op varchar(16) DEFAULT NULL COMMENT 'GT/GTE/LT/LTE/EQ',
  right_formula_json longtext COMMENT '公式判定-右公式',
  sort int DEFAULT 0,
  remark varchar(255) DEFAULT NULL,
  more longtext,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_passive_cond_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被动技能生效条件';

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
