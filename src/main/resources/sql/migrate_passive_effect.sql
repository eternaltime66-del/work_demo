-- 被动技能效果（基础属性：生命/防御/攻击 增减）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_passive_effect (
  id varchar(64) NOT NULL,
  skill_id varchar(64) NOT NULL COMMENT '被动技能id',
  attr_key varchar(32) NOT NULL COMMENT 'ATK/MAX_HP/DEF',
  attr_dir varchar(32) NOT NULL COMMENT 'INCREASE/DECREASE',
  value_num decimal(18,4) NOT NULL DEFAULT 0 COMMENT '增减数值',
  sort int DEFAULT 0,
  remark varchar(255) DEFAULT NULL,
  more longtext,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_passive_effect_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被动技能效果';
