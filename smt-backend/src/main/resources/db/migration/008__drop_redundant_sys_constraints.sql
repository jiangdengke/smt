-- 移除冗余的复合唯一约束（主键已覆盖）

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_workshop_id_factory')
    ALTER TABLE smtBackend.sys_workshop DROP CONSTRAINT [UQ_sys_workshop_id_factory];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_line_id_workshop')
    ALTER TABLE smtBackend.sys_line DROP CONSTRAINT [UQ_sys_line_id_workshop];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_machine_id_model')
    ALTER TABLE smtBackend.sys_machine DROP CONSTRAINT [UQ_sys_machine_id_model];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_abnormal_type_id_category')
    ALTER TABLE smtBackend.sys_abnormal_type DROP CONSTRAINT [UQ_sys_abnormal_type_id_category];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_person_id_team')
    ALTER TABLE smtBackend.sys_person DROP CONSTRAINT [UQ_sys_person_id_team];
