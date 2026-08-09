-- 闪避改为 BuffKind.DODGE：由 APPEND_BUFF 挂载，配置在 app_buff_def
ALTER TABLE app_buff_def
  ADD COLUMN dodge_chance int NULL COMMENT '闪避概率%（持有时）' AFTER right_formula_json,
  ADD COLUMN skill_match_mode varchar(32) NULL COMMENT '闪避：技能匹配模式' AFTER dodge_chance,
  ADD COLUMN match_skill_school varchar(64) NULL COMMENT '闪避：指定流派' AFTER skill_match_mode,
  ADD COLUMN match_skill_type varchar(32) NULL COMMENT '闪避：指定技能类型' AFTER match_skill_school,
  ADD COLUMN match_damage_element varchar(32) NULL COMMENT '闪避：指定元素' AFTER match_skill_type,
  ADD COLUMN match_skill_id varchar(64) NULL COMMENT '闪避：指定技能ID' AFTER match_damage_element;

-- 若曾把闪避做在 SkillOutput 上，清理旧列（无数据可直接 DROP）
-- ALTER TABLE app_skill_output DROP COLUMN dodge_chance, DROP COLUMN skill_match_mode,
--   DROP COLUMN match_skill_school, DROP COLUMN match_skill_type,
--   DROP COLUMN match_damage_element, DROP COLUMN match_skill_id;
