-- 技能字段扩展 + 快捷充能/效果组
SET NAMES utf8mb4;

-- app_active_skill 扩展字段（若已存在可忽略报错）
-- need_charge / max_cast_* / formula_json
ALTER TABLE app_active_skill
  ADD COLUMN formula_json longtext NULL COMMENT '公式编辑器 token JSON' AFTER more;

ALTER TABLE app_active_skill
  ADD COLUMN need_charge_mode varchar(32) DEFAULT 'MANUAL' COMMENT '所需充能方式 SELF_BASE_ACTION/MANUAL' AFTER need_charge;

CREATE TABLE IF NOT EXISTS app_skill_charge_group (
  id varchar(64) NOT NULL,
  name varchar(255) DEFAULT NULL COMMENT '充能组名称',
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  more longtext COMMENT '扩展JSON(条件细节后续补充)',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快捷充能组';

CREATE TABLE IF NOT EXISTS app_skill_effect_group (
  id varchar(64) NOT NULL,
  name varchar(255) DEFAULT NULL COMMENT '效果组名称',
  target_type varchar(32) DEFAULT NULL COMMENT '目标',
  effect_type varchar(32) DEFAULT NULL COMMENT '效果类型 DAMAGE/HEAL/ATTR_MODIFY',
  formula_json longtext COMMENT '伤害/治疗公式 token JSON',
  hit_segments int DEFAULT 1 COMMENT '段数',
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  more longtext COMMENT '扩展JSON',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快捷效果组';

-- 已有表可执行（列已存在则忽略报错）
ALTER TABLE app_skill_effect_group
  ADD COLUMN target_type varchar(32) DEFAULT NULL COMMENT '目标' AFTER name,
  ADD COLUMN effect_type varchar(32) DEFAULT NULL COMMENT '效果类型' AFTER target_type,
  ADD COLUMN formula_json longtext NULL COMMENT '伤害/治疗公式' AFTER effect_type,
  ADD COLUMN hit_segments int DEFAULT 1 COMMENT '段数' AFTER formula_json;

ALTER TABLE app_skill_effect
  ADD COLUMN target_type varchar(32) DEFAULT NULL COMMENT '目标' AFTER name,
  ADD COLUMN effect_type varchar(32) DEFAULT NULL COMMENT '效果类型' AFTER target_type,
  ADD COLUMN formula_json longtext NULL COMMENT '伤害/治疗公式' AFTER effect_type,
  ADD COLUMN hit_segments int DEFAULT 1 COMMENT '段数' AFTER formula_json;

CREATE TABLE IF NOT EXISTS app_player_role_skill (
  id varchar(64) NOT NULL,
  role_id varchar(64) NOT NULL COMMENT '玩家角色id',
  skill_id varchar(64) NOT NULL COMMENT '技能id',
  sort int DEFAULT 0,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_role (role_id),
  KEY idx_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家角色持有技能';
