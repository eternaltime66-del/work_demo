-- 主角占地统一为横 2 格、竖 3 格。
UPDATE app_player_role
SET grid_w = 2,
    grid_h = 3,
    UPDATE_TIME = NOW()
WHERE main_role = 1 OR role_category = 'HERO';
