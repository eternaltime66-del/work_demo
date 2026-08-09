SET NAMES utf8mb4;
SET @ask := 'ASK_10000001';
SET @sch := 'SCH_10000001';
SET @sout := 'SOUT_10000001';
SET @sef := 'SEF_10000001';
SET @formula := CONCAT(
  '[{"kind":"PARAM","paramMode":"READ","readRole":"SELF","readKey":"ATK"},',
  '{"kind":"OP","op":"*"},',
  '{"kind":"PARAM","paramMode":"LITERAL","value":"1"}]'
);

DELETE FROM app_skill_output WHERE skill_id = @ask OR id = @sout;
DELETE FROM app_skill_effect WHERE skill_id = @ask OR id = @sef;
DELETE FROM app_skill_charge WHERE skill_id = @ask OR id = @sch;
DELETE FROM app_active_skill WHERE code = 'DEFAULT_NORMAL' OR id = @ask;

INSERT INTO app_active_skill (
  id, name, skill_type, skill_school, damage_element, code,
  need_charge, need_charge_mode, max_cast_skill, max_cast_global, max_cast_all_means, max_cast_role,
  sort, enable, remark, CREATE_TIME, UPDATE_TIME
) VALUES (
  @ask, '普攻', 'NORMAL', '无', 'PHYSICAL', 'DEFAULT_NORMAL',
  0, 'SELF_BASE_ACTION', 0, 0, 0, 0,
  0, 1, '系统默认普攻：所需=自身行动值；每1行动值+1充能', NOW(), NOW()
);

INSERT INTO app_skill_charge (
  id, skill_id, name, condition_type, scope, every_action_value, charge_gain, sort, remark, CREATE_TIME, UPDATE_TIME
) VALUES (
  @sch, @ask, '行动充能', 'ACTION_VALUE', 'GLOBAL', 1, 1, 0, '普攻默认：每1行动值+1充能', NOW(), NOW()
);

INSERT INTO app_skill_output (
  id, skill_id, name, output_kind, target_type, effect_type, damage_element,
  formula_json, hit_segments, trigger_rate, duration_av, sort, create_time, update_time
) VALUES (
  @sout, @ask, '对战场首位敌方造成伤害', 'EFFECT', 'FIRST', 'DAMAGE', 'PHYSICAL',
  @formula, 1, 100, 0, 0, NOW(), NOW()
);

INSERT INTO app_skill_effect (
  id, skill_id, name, target_type, effect_type, formula_json, hit_segments, trigger_rate, duration_av, sort, CREATE_TIME, UPDATE_TIME
) VALUES (
  @sef, @ask, '普攻伤害', 'FIRST', 'DAMAGE', @formula, 1, 100, 0, 0, NOW(), NOW()
);

UPDATE app_player_role_skill prs
LEFT JOIN app_active_skill ask ON ask.id = prs.skill_id
SET prs.skill_id = @ask, prs.UPDATE_TIME = NOW()
WHERE ask.id IS NULL;

INSERT INTO app_player_role_skill (id, role_id, skill_id, sort, CREATE_TIME, UPDATE_TIME)
SELECT CONCAT('PRS_', LPAD(FLOOR(RAND() * 100000000), 8, '0')), pr.id, @ask, 0, NOW(), NOW()
FROM app_player_role pr
WHERE NOT EXISTS (
  SELECT 1 FROM app_player_role_skill prs
  JOIN app_active_skill ask ON ask.id = prs.skill_id
  WHERE prs.role_id = pr.id AND ask.skill_type = 'NORMAL'
);

SELECT id, code, name, skill_type, need_charge_mode, enable FROM app_active_skill WHERE code = 'DEFAULT_NORMAL';
SELECT id, name, condition_type, every_action_value, charge_gain FROM app_skill_charge WHERE skill_id = @ask;
SELECT id, name, output_kind, target_type FROM app_skill_output WHERE skill_id = @ask;
SELECT COUNT(*) AS roles_bound FROM app_player_role_skill WHERE skill_id = @ask;
