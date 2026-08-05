-- 装备充能技能槽 + 默认技能
SET NAMES utf8mb4;

ALTER TABLE app_item
  ADD COLUMN charge_skill_slot_count int DEFAULT 0 COMMENT '充能技能槽数量' AFTER remark,
  ADD COLUMN player_can_edit_skill_slot tinyint(1) DEFAULT 0 COMMENT '玩家是否可编辑技能槽' AFTER charge_skill_slot_count;

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
