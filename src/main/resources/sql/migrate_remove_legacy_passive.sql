-- 技能 V2 收口：删除已废弃的战斗被动数据与结构
SET NAMES utf8mb4;

DELETE bind_row
FROM app_item_default_passive bind_row
JOIN app_passive_skill skill ON skill.id = bind_row.passive_skill_id
WHERE skill.passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED');

DELETE cond_row
FROM app_passive_condition cond_row
JOIN app_passive_skill skill ON skill.id = cond_row.skill_id
WHERE skill.passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED');

DELETE effect_row
FROM app_passive_effect effect_row
JOIN app_passive_skill skill ON skill.id = effect_row.skill_id
WHERE skill.passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED');

DELETE output_row
FROM app_skill_output output_row
JOIN app_passive_skill skill ON skill.id = output_row.passive_skill_id
WHERE skill.passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED');

DELETE FROM app_passive_skill
WHERE passive_type IN ('IN_ANCHOR', 'IN_PERIODIC', 'IN_SUSTAINED');

DROP TABLE IF EXISTS app_passive_combat_effect;

ALTER TABLE app_passive_skill
  DROP COLUMN anchor_type,
  DROP COLUMN periodic_trigger_mode;

ALTER TABLE app_item
  DROP COLUMN anchor_passive_slot_count,
  DROP COLUMN player_default_edit_anchor_passive_slot_count,
  DROP COLUMN player_max_edit_anchor_passive_slot_count,
  DROP COLUMN periodic_passive_slot_count,
  DROP COLUMN player_default_edit_periodic_passive_slot_count,
  DROP COLUMN player_max_edit_periodic_passive_slot_count,
  DROP COLUMN sustained_passive_slot_count,
  DROP COLUMN player_default_edit_sustained_passive_slot_count,
  DROP COLUMN player_max_edit_sustained_passive_slot_count;
