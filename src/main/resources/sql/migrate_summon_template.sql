-- 角色模板分类 + 召唤物继承召唤者基础属性比例（单位 1%）
-- 若列已存在请跳过对应 ADD

ALTER TABLE app_role_base_stat
  ADD COLUMN role_category varchar(32) NULL COMMENT 'HERO/PARTNER/SUMMON' AFTER main_role,
  ADD COLUMN inherit_atk_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者攻击%' AFTER role_category,
  ADD COLUMN inherit_def_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者防御%' AFTER inherit_atk_ratio,
  ADD COLUMN inherit_hp_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者生命%' AFTER inherit_def_ratio;

UPDATE app_role_base_stat SET role_category = 'HERO' WHERE role_category IS NULL;

ALTER TABLE app_player_role
  ADD COLUMN inherit_atk_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者攻击%' AFTER role_category,
  ADD COLUMN inherit_def_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者防御%' AFTER inherit_atk_ratio,
  ADD COLUMN inherit_hp_ratio decimal(20,8) NULL COMMENT '召唤物：继承召唤者生命%' AFTER inherit_def_ratio;
