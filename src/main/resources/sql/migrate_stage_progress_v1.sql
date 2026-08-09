-- stage progress / stamina / first reward / tower run
SET NAMES utf8mb4;

-- app_stage columns (ignore error if already exist)
-- ALTER TABLE app_stage ADD COLUMN stamina_cost INT NULL DEFAULT 1;
-- ALTER TABLE app_stage ADD COLUMN daily_max_attempts INT NULL DEFAULT NULL;

CREATE TABLE IF NOT EXISTS app_stage_first_reward (
  id          VARCHAR(64)  NOT NULL PRIMARY KEY,
  stage_id    VARCHAR(64)  NOT NULL,
  item_id     VARCHAR(64)  NOT NULL,
  qty         INT          NOT NULL DEFAULT 1,
  sort        INT          NULL DEFAULT 0,
  CREATE_TIME DATETIME     NULL,
  UPDATE_TIME DATETIME     NULL,
  KEY idx_sfr_stage (stage_id),
  KEY idx_sfr_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_player_stamina (
  id          VARCHAR(64)  NOT NULL PRIMARY KEY,
  uid         VARCHAR(64)  NOT NULL,
  stamina     INT          NOT NULL DEFAULT 30,
  max_stamina INT          NOT NULL DEFAULT 30,
  CREATE_TIME DATETIME     NULL,
  UPDATE_TIME DATETIME     NULL,
  UNIQUE KEY uk_ps_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_player_stage_level (
  id                    VARCHAR(64)  NOT NULL PRIMARY KEY,
  uid                   VARCHAR(64)  NOT NULL,
  level_id              VARCHAR(64)  NOT NULL,
  cleared               TINYINT(1)   NOT NULL DEFAULT 0,
  first_reward_claimed  TINYINT(1)   NOT NULL DEFAULT 0,
  attempt_date          DATE         NULL,
  attempt_count         INT          NOT NULL DEFAULT 0,
  CREATE_TIME           DATETIME     NULL,
  UPDATE_TIME           DATETIME     NULL,
  UNIQUE KEY uk_psl_uid_level (uid, level_id),
  KEY idx_psl_uid (uid),
  KEY idx_psl_level (level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_player_stage_chapter (
  id                    VARCHAR(64)  NOT NULL PRIMARY KEY,
  uid                   VARCHAR(64)  NOT NULL,
  chapter_id            VARCHAR(64)  NOT NULL,
  cleared               TINYINT(1)   NOT NULL DEFAULT 0,
  first_reward_claimed  TINYINT(1)   NOT NULL DEFAULT 0,
  CREATE_TIME           DATETIME     NULL,
  UPDATE_TIME           DATETIME     NULL,
  UNIQUE KEY uk_psc_uid_chapter (uid, chapter_id),
  KEY idx_psc_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_player_tower_run (
  id                VARCHAR(64)  NOT NULL PRIMARY KEY,
  uid               VARCHAR(64)  NOT NULL,
  status            VARCHAR(32)  NOT NULL DEFAULT 'RUNNING',
  current_level_id  VARCHAR(64)  NULL,
  ally_hp_json      TEXT         NULL,
  CREATE_TIME       DATETIME     NULL,
  UPDATE_TIME       DATETIME     NULL,
  UNIQUE KEY uk_ptr_uid (uid),
  KEY idx_ptr_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME)
SELECT 'STY_TOWER_01', NULL, 'TYPE', '无尽塔', 'TOWER', 10, 1, 'tower mode', NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM app_stage WHERE code = 'TOWER');
