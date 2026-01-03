-- 去除机型维度，机台号改为按线别联动

-- 1) sys_machine 增加 line_id 并移除 model_id
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_sys_machine_model')
    ALTER TABLE smtBackend.sys_machine DROP CONSTRAINT [FK_sys_machine_model];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_machine_model_no')
    ALTER TABLE smtBackend.sys_machine DROP CONSTRAINT [UQ_sys_machine_model_no];

IF COL_LENGTH('smtBackend.sys_machine', 'line_id') IS NULL
    ALTER TABLE smtBackend.sys_machine ADD line_id BIGINT NULL;

-- 尝试基于维修记录回填线别
UPDATE sm
SET line_id = x.line_id
FROM smtBackend.sys_machine sm
CROSS APPLY (
    SELECT TOP 1 l.id AS line_id
    FROM smtBackend.repair_record rr
    JOIN smtBackend.sys_line l ON l.name = rr.line_name
    WHERE rr.machine_no = sm.machine_no
    ORDER BY rr.id DESC
) x
WHERE sm.line_id IS NULL;

IF COL_LENGTH('smtBackend.sys_machine', 'model_id') IS NOT NULL
    ALTER TABLE smtBackend.sys_machine DROP COLUMN model_id;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_sys_machine_line')
    ALTER TABLE smtBackend.sys_machine
        ADD CONSTRAINT [FK_sys_machine_line] FOREIGN KEY ([line_id]) REFERENCES smtBackend.sys_line([id]);

IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_machine_line_no')
    ALTER TABLE smtBackend.sys_machine
        ADD CONSTRAINT [UQ_sys_machine_line_no] UNIQUE ([line_id], [machine_no]);

IF COL_LENGTH('smtBackend.sys_machine', 'line_id') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM smtBackend.sys_machine WHERE line_id IS NULL)
        ALTER TABLE smtBackend.sys_machine ALTER COLUMN line_id BIGINT NOT NULL;
END

-- 2) 删除 sys_model 表
IF OBJECT_ID('smtBackend.sys_model', 'U') IS NOT NULL
    DROP TABLE smtBackend.sys_model;

-- 3) 维修记录移除机型快照字段
IF COL_LENGTH('smtBackend.repair_record', 'model_name') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN model_name;
