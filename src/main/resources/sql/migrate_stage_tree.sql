-- ============================================================
-- 关卡：三表(type/chapter/level) → 单表树 app_stage
-- kind: TYPE / CHAPTER / LEVEL；parent_id 指向父节点（根为 NULL）
-- 保留原 id，摆怪表 level_id 无需变更
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_stage (
  id            VARCHAR(64)  NOT NULL PRIMARY KEY,
  parent_id     VARCHAR(64)  NULL,
  kind          VARCHAR(32)  NOT NULL,
  name          VARCHAR(255) NULL,
  code          VARCHAR(64)  NULL,
  sort          INT          NULL DEFAULT 0,
  enable        TINYINT(1)   NULL DEFAULT 1,
  remark        VARCHAR(255) NULL,
  CREATE_TIME   DATETIME     NULL,
  UPDATE_TIME   DATETIME     NULL,
  KEY idx_stage_parent (parent_id),
  KEY idx_stage_kind (kind),
  KEY idx_stage_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 幂等：清空后重灌（仅当旧三表仍存在时由迁移脚本灌入）
DELETE FROM app_stage;

INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME)
SELECT id, NULL, 'TYPE', name, code, IFNULL(sort, 0), IFNULL(enable, 1), remark, CREATE_TIME, UPDATE_TIME
FROM app_stage_type;

INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME)
SELECT id, type_id, 'CHAPTER', name, code, IFNULL(sort, 0), IFNULL(enable, 1), remark, CREATE_TIME, UPDATE_TIME
FROM app_stage_chapter;

INSERT INTO app_stage (id, parent_id, kind, name, code, sort, enable, remark, CREATE_TIME, UPDATE_TIME)
SELECT id, chapter_id, 'LEVEL', name, code, IFNULL(sort, 0), IFNULL(enable, 1), remark, CREATE_TIME, UPDATE_TIME
FROM app_stage_level;

DROP TABLE IF EXISTS app_stage_level;
DROP TABLE IF EXISTS app_stage_chapter;
DROP TABLE IF EXISTS app_stage_type;
