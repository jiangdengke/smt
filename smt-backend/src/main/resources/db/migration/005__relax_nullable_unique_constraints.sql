-- 允许可空编码/工号重复 NULL：将唯一约束改为过滤唯一索引

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_factory_code')
    ALTER TABLE smtBackend.sys_factory DROP CONSTRAINT [UQ_sys_factory_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_workshop_code')
    ALTER TABLE smtBackend.sys_workshop DROP CONSTRAINT [UQ_sys_workshop_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_line_code')
    ALTER TABLE smtBackend.sys_line DROP CONSTRAINT [UQ_sys_line_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_model_code')
    ALTER TABLE smtBackend.sys_model DROP CONSTRAINT [UQ_sys_model_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_abnormal_category_code')
    ALTER TABLE smtBackend.sys_abnormal_category DROP CONSTRAINT [UQ_sys_abnormal_category_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_abnormal_type_code')
    ALTER TABLE smtBackend.sys_abnormal_type DROP CONSTRAINT [UQ_sys_abnormal_type_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_team_code')
    ALTER TABLE smtBackend.sys_team DROP CONSTRAINT [UQ_sys_team_code];

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_sys_person_employee_no')
    ALTER TABLE smtBackend.sys_person DROP CONSTRAINT [UQ_sys_person_employee_no];

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_factory_code'
       AND object_id = OBJECT_ID('smtBackend.sys_factory')
)
    CREATE UNIQUE INDEX [UX_sys_factory_code]
        ON smtBackend.sys_factory ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_workshop_code'
       AND object_id = OBJECT_ID('smtBackend.sys_workshop')
)
    CREATE UNIQUE INDEX [UX_sys_workshop_code]
        ON smtBackend.sys_workshop ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_line_code'
       AND object_id = OBJECT_ID('smtBackend.sys_line')
)
    CREATE UNIQUE INDEX [UX_sys_line_code]
        ON smtBackend.sys_line ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_model_code'
       AND object_id = OBJECT_ID('smtBackend.sys_model')
)
    CREATE UNIQUE INDEX [UX_sys_model_code]
        ON smtBackend.sys_model ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_abnormal_category_code'
       AND object_id = OBJECT_ID('smtBackend.sys_abnormal_category')
)
    CREATE UNIQUE INDEX [UX_sys_abnormal_category_code]
        ON smtBackend.sys_abnormal_category ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_abnormal_type_code'
       AND object_id = OBJECT_ID('smtBackend.sys_abnormal_type')
)
    CREATE UNIQUE INDEX [UX_sys_abnormal_type_code]
        ON smtBackend.sys_abnormal_type ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_team_code'
       AND object_id = OBJECT_ID('smtBackend.sys_team')
)
    CREATE UNIQUE INDEX [UX_sys_team_code]
        ON smtBackend.sys_team ([code])
        WHERE [code] IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
     WHERE name = 'UX_sys_person_employee_no'
       AND object_id = OBJECT_ID('smtBackend.sys_person')
)
    CREATE UNIQUE INDEX [UX_sys_person_employee_no]
        ON smtBackend.sys_person ([employee_no])
        WHERE [employee_no] IS NOT NULL;
