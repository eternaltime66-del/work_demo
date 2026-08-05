-- 技能效果 / 快捷效果组：属性修改（attr_key + attr_dir）
ALTER TABLE app_skill_effect
  ADD COLUMN attr_key varchar(32) NULL COMMENT 'ATTR_MODIFY: ATK/MAX_HP/DEF' AFTER effect_type,
  ADD COLUMN attr_dir varchar(32) NULL COMMENT 'ATTR_MODIFY: INCREASE/DECREASE' AFTER attr_key;

ALTER TABLE app_skill_effect_group
  ADD COLUMN attr_key varchar(32) NULL COMMENT 'ATTR_MODIFY: ATK/MAX_HP/DEF' AFTER effect_type,
  ADD COLUMN attr_dir varchar(32) NULL COMMENT 'ATTR_MODIFY: INCREASE/DECREASE' AFTER attr_key;
