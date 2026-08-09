-- TAKE_ANY_DAMAGE / DEAL_ANY_DAMAGE → SKILL_CHARGE + TAKE_DAMAGE / DEAL_DAMAGE
-- DEAL_KILL → SKILL_CHARGE + KILL（若仍有旧数据）
SET NAMES utf8mb4;

UPDATE app_skill_charge
SET condition_type = 'SKILL_CHARGE',
    skill_charge_event = 'TAKE_DAMAGE',
    skill_charge_match = IFNULL(skill_charge_match, 'ANY')
WHERE condition_type = 'TAKE_ANY_DAMAGE';

UPDATE app_skill_charge
SET condition_type = 'SKILL_CHARGE',
    skill_charge_event = 'DEAL_DAMAGE',
    skill_charge_match = IFNULL(skill_charge_match, 'ANY')
WHERE condition_type = 'DEAL_ANY_DAMAGE';

UPDATE app_skill_charge
SET condition_type = 'SKILL_CHARGE',
    skill_charge_event = 'KILL',
    skill_charge_match = IFNULL(skill_charge_match, 'ANY')
WHERE condition_type = 'DEAL_KILL';
