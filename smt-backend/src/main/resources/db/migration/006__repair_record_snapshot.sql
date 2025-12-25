-- 维修记录快照字段：记录名称，解除与维护表的强耦合

IF COL_LENGTH('smtBackend.repair_record', 'factory_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD factory_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'workshop_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD workshop_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'line_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD line_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'model_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD model_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'machine_no') IS NULL
    ALTER TABLE smtBackend.repair_record ADD machine_no NVARCHAR(64) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'abnormal_category_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD abnormal_category_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'abnormal_type_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD abnormal_type_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'team_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD team_name NVARCHAR(255) NULL;
IF COL_LENGTH('smtBackend.repair_record', 'responsible_person_name') IS NULL
    ALTER TABLE smtBackend.repair_record ADD responsible_person_name NVARCHAR(255) NULL;

IF COL_LENGTH('smtBackend.repair_record_person', 'person_name') IS NULL
    ALTER TABLE smtBackend.repair_record_person ADD person_name NVARCHAR(255) NULL;

-- 回填名称快照
UPDATE rr
SET factory_name = f.name,
    workshop_name = w.name,
    line_name = l.name,
    model_name = m.name,
    machine_no = mc.machine_no,
    abnormal_category_name = ac.name,
    abnormal_type_name = at.name,
    team_name = t.name,
    responsible_person_name = p.name
FROM smtBackend.repair_record rr
LEFT JOIN smtBackend.sys_factory f ON rr.factory_id = f.id
LEFT JOIN smtBackend.sys_workshop w ON rr.workshop_id = w.id
LEFT JOIN smtBackend.sys_line l ON rr.line_id = l.id
LEFT JOIN smtBackend.sys_model m ON rr.model_id = m.id
LEFT JOIN smtBackend.sys_machine mc ON rr.machine_id = mc.id
LEFT JOIN smtBackend.sys_abnormal_category ac ON rr.abnormal_category_id = ac.id
LEFT JOIN smtBackend.sys_abnormal_type at ON rr.abnormal_type_id = at.id
LEFT JOIN smtBackend.sys_team t ON rr.team_id = t.id
LEFT JOIN smtBackend.sys_person p ON rr.responsible_person_id = p.id;

UPDATE rrp
SET person_name = p.name
FROM smtBackend.repair_record_person rrp
LEFT JOIN smtBackend.sys_person p ON rrp.person_id = p.id;

-- 删除维修记录到维护表的外键，解除删除限制
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_workshop')
    ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_workshop];
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_line')
    ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_line];
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_machine')
    ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_machine];
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_abnormal_type')
    ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_abnormal_type];
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_responsible_person')
    ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_responsible_person];
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_repair_record_person_person')
    ALTER TABLE smtBackend.repair_record_person DROP CONSTRAINT [FK_repair_record_person_person];
