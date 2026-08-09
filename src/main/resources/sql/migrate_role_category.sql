-- 玩家/怪物角色分类（召唤体系）
ALTER TABLE app_player_role
  ADD COLUMN IF NOT EXISTS role_category VARCHAR(32) NOT NULL DEFAULT 'PARTNER'
  COMMENT '角色分类 HERO/PARTNER/SUMMON' AFTER main_role;

UPDATE app_player_role SET role_category = 'HERO' WHERE main_role = 1 AND (role_category IS NULL OR role_category = '' OR role_category = 'PARTNER');

ALTER TABLE app_monster
  ADD COLUMN IF NOT EXISTS role_category VARCHAR(32) NOT NULL DEFAULT 'MONSTER'
  COMMENT '角色分类 MONSTER/SUMMON' AFTER rarity;
