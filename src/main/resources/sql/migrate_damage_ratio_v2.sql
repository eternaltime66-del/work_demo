-- 伤害比例补强：元素/物理渠道（默认 100%，乘法叠乘）
ALTER TABLE app_role_base_stat
  ADD COLUMN deal_element_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '造成元素伤害比例(单位1%)' AFTER taken_dmg_ratio,
  ADD COLUMN taken_element_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '受到元素伤害比例(单位1%)' AFTER deal_element_dmg_ratio,
  ADD COLUMN deal_phys_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '造成物理伤害比例(单位1%)' AFTER taken_element_dmg_ratio,
  ADD COLUMN taken_phys_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '受到物理伤害比例(单位1%)' AFTER deal_phys_dmg_ratio;

ALTER TABLE app_player_role
  ADD COLUMN deal_element_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '造成元素伤害比例(单位1%)' AFTER taken_dmg_ratio,
  ADD COLUMN taken_element_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '受到元素伤害比例(单位1%)' AFTER deal_element_dmg_ratio,
  ADD COLUMN deal_phys_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '造成物理伤害比例(单位1%)' AFTER taken_element_dmg_ratio,
  ADD COLUMN taken_phys_dmg_ratio decimal(20,8) DEFAULT 100 COMMENT '受到物理伤害比例(单位1%)' AFTER deal_phys_dmg_ratio;

UPDATE app_role_base_stat SET
  deal_element_dmg_ratio = IFNULL(deal_element_dmg_ratio, 100),
  taken_element_dmg_ratio = IFNULL(taken_element_dmg_ratio, 100),
  deal_phys_dmg_ratio = IFNULL(deal_phys_dmg_ratio, 100),
  taken_phys_dmg_ratio = IFNULL(taken_phys_dmg_ratio, 100);

UPDATE app_player_role SET
  deal_element_dmg_ratio = IFNULL(deal_element_dmg_ratio, 100),
  taken_element_dmg_ratio = IFNULL(taken_element_dmg_ratio, 100),
  deal_phys_dmg_ratio = IFNULL(deal_phys_dmg_ratio, 100),
  taken_phys_dmg_ratio = IFNULL(taken_phys_dmg_ratio, 100);
