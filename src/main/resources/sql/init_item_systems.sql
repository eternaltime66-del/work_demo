-- 物品 / 装备 / 仓库 / 背包 / 配方 / 掉落
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_item (
  id varchar(64) NOT NULL COMMENT 'ID',
  code varchar(64) DEFAULT NULL COMMENT '物品编码',
  name varchar(255) DEFAULT NULL COMMENT '名称',
  icon varchar(512) DEFAULT NULL COMMENT '图标',
  item_type varchar(32) NOT NULL COMMENT 'MATERIAL/WEAPON/ARMOR/GLOVES/HELMET/ACCESSORY/LEGS',
  max_stack int DEFAULT 99 COMMENT '最大堆叠',
  sort int DEFAULT 0 COMMENT '排序',
  enable tinyint(1) DEFAULT 1 COMMENT '启用',
  remark varchar(255) DEFAULT NULL,
  charge_skill_slot_count int DEFAULT 0 COMMENT '默认充能技能槽数量',
  player_default_edit_charge_skill_slot_count int DEFAULT 0 COMMENT '玩家默认可编辑充能技能槽数量',
  player_max_edit_charge_skill_slot_count int DEFAULT 0 COMMENT '玩家最大可编辑充能技能槽数量',
  player_can_edit_skill_slot tinyint(1) DEFAULT 0 COMMENT '玩家是否可编辑技能槽',
  basic_passive_slot_count int DEFAULT 0 COMMENT '默认自带基础被动数量',
  player_default_edit_basic_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑基础被动数量',
  player_max_edit_basic_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大基础被动数量',
  advanced_passive_slot_count int DEFAULT 0 COMMENT '默认自带高级属性被动数量',
  player_default_edit_advanced_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑高级属性被动数量',
  player_max_edit_advanced_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大高级属性被动数量',
  anchor_passive_slot_count int DEFAULT 0 COMMENT '默认自带锚点被动数量',
  player_default_edit_anchor_passive_slot_count int DEFAULT 0 COMMENT '默认可编辑锚点被动数量',
  player_max_edit_anchor_passive_slot_count int DEFAULT 0 COMMENT '可编辑最大锚点被动数量',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  KEY idx_item_type (item_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品总表';

CREATE TABLE IF NOT EXISTS app_item_material (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  grade int DEFAULT 1 COMMENT '品级',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料表';

CREATE TABLE IF NOT EXISTS app_item_weapon (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  base_atk int DEFAULT 0 COMMENT '攻击',
  atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)',
  atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)',
  normal_skill_id varchar(64) DEFAULT NULL COMMENT '普攻技能id（有则替换角色普攻）',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='武器表';

CREATE TABLE IF NOT EXISTS app_item_armor (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  hp int DEFAULT 0 COMMENT '生命',
  defense int DEFAULT 0 COMMENT '防御',
  atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)',
  atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护甲表';

CREATE TABLE IF NOT EXISTS app_item_gloves (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  hp int DEFAULT 0 COMMENT '生命',
  defense int DEFAULT 0 COMMENT '防御',
  atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)',
  atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护手表';

CREATE TABLE IF NOT EXISTS app_item_helmet (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  hp int DEFAULT 0 COMMENT '生命',
  defense int DEFAULT 0 COMMENT '防御',
  atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)',
  atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='头盔表';

CREATE TABLE IF NOT EXISTS app_item_accessory (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饰品表';

CREATE TABLE IF NOT EXISTS app_item_legs (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  hp int DEFAULT 0 COMMENT '生命',
  defense int DEFAULT 0 COMMENT '防御',
  atk_speed_up_ratio decimal(12, 4) DEFAULT 0 COMMENT '增加攻速(%)',
  atk_speed_down_ratio decimal(12, 4) DEFAULT 0 COMMENT '减少攻速(%)',
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护腿表';

CREATE TABLE IF NOT EXISTS app_item_default_skill (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品主表id',
  skill_id varchar(64) NOT NULL COMMENT '主动技能id',
  slot_no int DEFAULT 0 COMMENT '槽位序号从0开始',
  sort int DEFAULT 0 COMMENT '排序',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_slot (item_id, slot_no),
  KEY idx_item (item_id),
  KEY idx_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备默认充能技能';

CREATE TABLE IF NOT EXISTS app_warehouse (
  id varchar(64) NOT NULL COMMENT 'ID',
  uid varchar(64) NOT NULL COMMENT '玩家uid',
  max_slots int DEFAULT 100 COMMENT '最大格数',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家仓库';

CREATE TABLE IF NOT EXISTS app_warehouse_item (
  id varchar(64) NOT NULL COMMENT 'ID',
  uid varchar(64) NOT NULL COMMENT '玩家uid',
  warehouse_id varchar(64) NOT NULL COMMENT '仓库id',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  quantity int DEFAULT 0 COMMENT '数量',
  slot_no int DEFAULT NULL COMMENT '格子号',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_uid (uid),
  KEY idx_warehouse (warehouse_id),
  KEY idx_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库物品';

CREATE TABLE IF NOT EXISTS app_battle_bag (
  id varchar(64) NOT NULL COMMENT 'ID',
  uid varchar(64) NOT NULL COMMENT '玩家uid',
  item_id varchar(64) NOT NULL COMMENT '物品id',
  quantity int DEFAULT 0 COMMENT '数量',
  sort int DEFAULT 0 COMMENT '排序',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uid_item (uid, item_id),
  KEY idx_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战斗背包';

CREATE TABLE IF NOT EXISTS app_recipe (
  id varchar(64) NOT NULL COMMENT 'ID',
  name varchar(255) DEFAULT NULL COMMENT '配方名',
  output_item_id varchar(64) NOT NULL COMMENT '产出物品',
  output_qty int DEFAULT 1 COMMENT '产出数量',
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_output (output_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配方表';

CREATE TABLE IF NOT EXISTS app_recipe_material (
  id varchar(64) NOT NULL COMMENT 'ID',
  recipe_id varchar(64) NOT NULL COMMENT '配方id',
  item_id varchar(64) NOT NULL COMMENT '材料物品id',
  quantity int DEFAULT 1 COMMENT '数量',
  sort int DEFAULT 0,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_recipe (recipe_id),
  KEY idx_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配方材料表';

CREATE TABLE IF NOT EXISTS app_item_default_passive (
  id varchar(64) NOT NULL COMMENT 'ID',
  item_id varchar(64) NOT NULL COMMENT '物品主表id',
  passive_skill_id varchar(64) NOT NULL COMMENT '被动技能id',
  passive_type varchar(32) NOT NULL COMMENT 'OUT_BASIC/OUT_ADVANCED',
  slot_no int DEFAULT 0 COMMENT '槽位序号从0开始',
  sort int DEFAULT 0 COMMENT '排序',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_item_type_slot (item_id, passive_type, slot_no),
  KEY idx_item (item_id),
  KEY idx_passive (passive_skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备默认被动技能';

CREATE TABLE IF NOT EXISTS app_monster_drop (
  id varchar(64) NOT NULL COMMENT 'ID',
  monster_id varchar(64) NOT NULL COMMENT '怪物id',
  item_id varchar(64) NOT NULL COMMENT '掉落物品id',
  drop_rate decimal(20,8) DEFAULT 0 COMMENT '掉落率(1%，100=100%；各配置独立判定)',
  min_qty int DEFAULT 1 COMMENT '最少数量(非线性随机偏小值)',
  max_qty int DEFAULT 1 COMMENT '最多数量',
  sort int DEFAULT 0,
  enable tinyint(1) DEFAULT 1,
  remark varchar(255) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_monster (monster_id),
  KEY idx_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怪物掉落配置';

-- 种子材料
INSERT INTO app_item (id,code,name,icon,item_type,max_stack,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES
('ITM_10000001','slime_gel','史莱姆凝胶',NULL,'MATERIAL',99,1,1,'普通材料',NOW(),NOW()),
('ITM_10000002','wolf_fang','狼牙',NULL,'MATERIAL',99,2,1,'稀有材料',NOW(),NOW()),
('ITM_10000003','forest_core','森林之心',NULL,'MATERIAL',20,3,1,'BOSS材料',NOW(),NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO app_item_material (id,item_id,grade,remark,CREATE_TIME,UPDATE_TIME) VALUES
('MAT_10000001','ITM_10000001',1,NULL,NOW(),NOW()),
('MAT_10000002','ITM_10000002',2,NULL,NOW(),NOW()),
('MAT_10000003','ITM_10000003',3,NULL,NOW(),NOW())
ON DUPLICATE KEY UPDATE grade=VALUES(grade);

INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,CREATE_TIME,UPDATE_TIME)
SELECT 'MDP_10000001', id, 'ITM_10000001', 80, 1, 2, 1, 1, NOW(), NOW() FROM app_monster WHERE name='史莱姆' LIMIT 1
ON DUPLICATE KEY UPDATE drop_rate=VALUES(drop_rate);

INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,CREATE_TIME,UPDATE_TIME)
SELECT 'MDP_10000002', id, 'ITM_10000002', 50, 1, 1, 1, 1, NOW(), NOW() FROM app_monster WHERE name='狼骑兵' LIMIT 1
ON DUPLICATE KEY UPDATE drop_rate=VALUES(drop_rate);

INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,CREATE_TIME,UPDATE_TIME)
SELECT 'MDP_10000003', id, 'ITM_10000003', 100, 1, 1, 1, 1, NOW(), NOW() FROM app_monster WHERE name='森林领主' LIMIT 1
ON DUPLICATE KEY UPDATE drop_rate=VALUES(drop_rate);
