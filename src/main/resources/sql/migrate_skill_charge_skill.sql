-- 充能条件：技能充能（释放/受到 + 匹配范围）
ALTER TABLE app_skill_charge
  ADD COLUMN skill_charge_event varchar(32) DEFAULT NULL COMMENT 'SKILL_CHARGE: CAST/RECEIVE' AFTER charge_gain,
  ADD COLUMN skill_charge_match varchar(32) DEFAULT NULL COMMENT 'SKILL_CHARGE: ANY/ANY_TYPE/SPECIFIC' AFTER skill_charge_event,
  ADD COLUMN match_skill_type varchar(32) DEFAULT NULL COMMENT 'SKILL_CHARGE+ANY_TYPE: 技能类型' AFTER skill_charge_match,
  ADD COLUMN match_skill_id varchar(64) DEFAULT NULL COMMENT 'SKILL_CHARGE+SPECIFIC: 技能id' AFTER match_skill_type;
