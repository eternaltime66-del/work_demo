-- 充能技能：流派 + 元素类型；技能匹配扩展指定流派 / 指定元素
ALTER TABLE app_active_skill
  ADD COLUMN skill_school VARCHAR(64) NOT NULL DEFAULT '无' COMMENT '流派' AFTER skill_type,
  ADD COLUMN damage_element VARCHAR(32) NOT NULL DEFAULT 'PHYSICAL' COMMENT '元素类型' AFTER skill_school;

ALTER TABLE app_skill_charge
  ADD COLUMN match_skill_school VARCHAR(64) NULL COMMENT '指定流派' AFTER match_skill_type,
  ADD COLUMN match_damage_element VARCHAR(32) NULL COMMENT '指定元素类型' AFTER match_skill_school;

ALTER TABLE app_passive_skill
  ADD COLUMN ref_skill_school VARCHAR(64) NULL COMMENT '指定流派' AFTER ref_skill_type,
  ADD COLUMN ref_damage_element VARCHAR(32) NULL COMMENT '指定元素类型' AFTER ref_skill_school;
