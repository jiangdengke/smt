-- 维修记录与维护表彻底解耦：仅保存名称快照

-- 删除旧的记录表维度主数据（保留 sys_* 维护表）
IF OBJECT_ID('smtBackend.line', 'U') IS NOT NULL
    DROP TABLE smtBackend.line;
IF OBJECT_ID('smtBackend.workshop', 'U') IS NOT NULL
    DROP TABLE smtBackend.workshop;
IF OBJECT_ID('smtBackend.factory', 'U') IS NOT NULL
    DROP TABLE smtBackend.factory;
IF OBJECT_ID('smtBackend.machine', 'U') IS NOT NULL
    DROP TABLE smtBackend.machine;
IF OBJECT_ID('smtBackend.model', 'U') IS NOT NULL
    DROP TABLE smtBackend.model;
IF OBJECT_ID('smtBackend.abnormal_type', 'U') IS NOT NULL
    DROP TABLE smtBackend.abnormal_type;
IF OBJECT_ID('smtBackend.abnormal_category', 'U') IS NOT NULL
    DROP TABLE smtBackend.abnormal_category;
IF OBJECT_ID('smtBackend.person', 'U') IS NOT NULL
    DROP TABLE smtBackend.person;
IF OBJECT_ID('smtBackend.team', 'U') IS NOT NULL
    DROP TABLE smtBackend.team;

-- 移除 repair_record 对维护表的 ID 关联字段
IF COL_LENGTH('smtBackend.repair_record', 'factory_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN factory_id;
IF COL_LENGTH('smtBackend.repair_record', 'workshop_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN workshop_id;
IF COL_LENGTH('smtBackend.repair_record', 'line_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN line_id;
IF COL_LENGTH('smtBackend.repair_record', 'model_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN model_id;
IF COL_LENGTH('smtBackend.repair_record', 'machine_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN machine_id;
IF COL_LENGTH('smtBackend.repair_record', 'abnormal_category_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN abnormal_category_id;
IF COL_LENGTH('smtBackend.repair_record', 'abnormal_type_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN abnormal_type_id;
IF COL_LENGTH('smtBackend.repair_record', 'team_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN team_id;
IF COL_LENGTH('smtBackend.repair_record', 'responsible_person_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record DROP COLUMN responsible_person_id;

-- 名称快照改为必填
ALTER TABLE smtBackend.repair_record ALTER COLUMN factory_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN workshop_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN line_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN model_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN machine_no NVARCHAR(64) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN abnormal_category_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN abnormal_type_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN team_name NVARCHAR(255) NOT NULL;
ALTER TABLE smtBackend.repair_record ALTER COLUMN responsible_person_name NVARCHAR(255) NOT NULL;

-- 维修记录-人员关系表仅保留人员名称
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'PK_repair_record_person')
    ALTER TABLE smtBackend.repair_record_person DROP CONSTRAINT [PK_repair_record_person];

IF COL_LENGTH('smtBackend.repair_record_person', 'id') IS NULL
    ALTER TABLE smtBackend.repair_record_person ADD id BIGINT IDENTITY(1,1) NOT NULL;

IF COL_LENGTH('smtBackend.repair_record_person', 'person_id') IS NOT NULL
    ALTER TABLE smtBackend.repair_record_person DROP COLUMN person_id;

ALTER TABLE smtBackend.repair_record_person ALTER COLUMN person_name NVARCHAR(255) NOT NULL;

ALTER TABLE smtBackend.repair_record_person
    ADD CONSTRAINT [PK_repair_record_person] PRIMARY KEY ([id]);
