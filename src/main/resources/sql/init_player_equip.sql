-- 玩家装备（挂主角）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_player_equip (
  id varchar(64) NOT NULL COMMENT 'ID',
  uid varchar(64) NOT NULL COMMENT '玩家uid',
  weapon_item_id varchar(64) DEFAULT NULL COMMENT '武器',
  armor_item_id varchar(64) DEFAULT NULL COMMENT '护甲',
  gloves_item_id varchar(64) DEFAULT NULL COMMENT '护手',
  helmet_item_id varchar(64) DEFAULT NULL COMMENT '头盔',
  legs_item_id varchar(64) DEFAULT NULL COMMENT '护腿',
  accessory1_item_id varchar(64) DEFAULT NULL COMMENT '饰品1',
  accessory2_item_id varchar(64) DEFAULT NULL COMMENT '饰品2',
  accessory3_item_id varchar(64) DEFAULT NULL COMMENT '饰品3',
  CREATE_TIME datetime DEFAULT NULL,
  UPDATE_TIME datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家装备';
