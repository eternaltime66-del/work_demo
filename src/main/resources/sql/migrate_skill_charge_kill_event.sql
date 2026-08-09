-- DEAL_KILL → SKILL_CHARGE + skill_charge_event=KILL
SET NAMES utf8mb4;

UPDATE app_skill_charge
SET condition_type = 'SKILL_CHARGE',
    skill_charge_event = 'KILL',
    skill_charge_match = IFNULL(skill_charge_match, 'ANY')
WHERE condition_type = 'DEAL_KILL';
