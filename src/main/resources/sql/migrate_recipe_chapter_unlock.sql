-- 配方：章节锁定解锁
ALTER TABLE app_recipe
  ADD COLUMN unlock_chapter_id varchar(64) NULL COMMENT '解锁所需章节StageId；空=不限' AFTER enable;
