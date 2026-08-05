-- 装备默认基础/高级被动槽
SET NAMES utf8mb4;

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
