-- Neon Expedition generated-art binding. Safe to run repeatedly.
START TRANSACTION;
UPDATE app_item
SET icon = CONCAT('art/item/', code, '.png')
WHERE id LIKE 'ITM_N20_%';
COMMIT;
