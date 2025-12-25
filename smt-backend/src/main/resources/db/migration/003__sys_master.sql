-- 系统维护主数据表（下拉选项）

-- 厂区维护表
CREATE TABLE smtBackend.sys_factory
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 厂区名称
    [code]       NVARCHAR(64)  NULL, -- 厂区编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_factory] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_factory_name] UNIQUE ([name]),
    CONSTRAINT [UQ_sys_factory_code] UNIQUE ([code])
);

-- 车间维护表
CREATE TABLE smtBackend.sys_workshop
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [factory_id] BIGINT       NOT NULL, -- 厂区ID
    [name]       NVARCHAR(255) NOT NULL, -- 车间名称
    [code]       NVARCHAR(64)  NULL, -- 车间编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_workshop] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_workshop_factory] FOREIGN KEY ([factory_id]) REFERENCES smtBackend.sys_factory([id]),
    CONSTRAINT [UQ_sys_workshop_factory_name] UNIQUE ([factory_id], [name]),
    CONSTRAINT [UQ_sys_workshop_code] UNIQUE ([code])
);

-- 线别维护表
CREATE TABLE smtBackend.sys_line
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [workshop_id] BIGINT       NOT NULL, -- 车间ID
    [name]        NVARCHAR(255) NOT NULL, -- 线别名称
    [code]        NVARCHAR(64)  NULL, -- 线别编码
    [sort_order]  INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]      NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_line] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_line_workshop] FOREIGN KEY ([workshop_id]) REFERENCES smtBackend.sys_workshop([id]),
    CONSTRAINT [UQ_sys_line_workshop_name] UNIQUE ([workshop_id], [name]),
    CONSTRAINT [UQ_sys_line_code] UNIQUE ([code])
);

-- 机型维护表
CREATE TABLE smtBackend.sys_model
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 机型名称
    [code]       NVARCHAR(64)  NULL, -- 机型编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_model] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_model_name] UNIQUE ([name]),
    CONSTRAINT [UQ_sys_model_code] UNIQUE ([code])
);

-- 机台维护表
CREATE TABLE smtBackend.sys_machine
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [model_id]   BIGINT       NOT NULL, -- 机型ID
    [machine_no] NVARCHAR(64) NOT NULL, -- 机台号
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_machine] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_machine_model] FOREIGN KEY ([model_id]) REFERENCES smtBackend.sys_model([id]),
    CONSTRAINT [UQ_sys_machine_model_no] UNIQUE ([model_id], [machine_no])
);

-- 异常类别维护表
CREATE TABLE smtBackend.sys_abnormal_category
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 异常类别名称
    [code]       NVARCHAR(64)  NULL, -- 异常类别编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_abnormal_category] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_abnormal_category_name] UNIQUE ([name]),
    CONSTRAINT [UQ_sys_abnormal_category_code] UNIQUE ([code])
);

-- 异常分类维护表
CREATE TABLE smtBackend.sys_abnormal_type
(
    [id]                   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [abnormal_category_id] BIGINT       NOT NULL, -- 异常类别ID
    [name]                 NVARCHAR(255) NOT NULL, -- 异常分类名称
    [code]                 NVARCHAR(64)  NULL, -- 异常分类编码
    [sort_order]           INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]               NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_abnormal_type] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_abnormal_type_category]
        FOREIGN KEY ([abnormal_category_id]) REFERENCES smtBackend.sys_abnormal_category([id]),
    CONSTRAINT [UQ_sys_abnormal_type_category_name] UNIQUE ([abnormal_category_id], [name]),
    CONSTRAINT [UQ_sys_abnormal_type_code] UNIQUE ([code])
);

-- 组别维护表
CREATE TABLE smtBackend.sys_team
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 组别名称
    [code]       NVARCHAR(64)  NULL, -- 组别编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_team] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_team_name] UNIQUE ([name]),
    CONSTRAINT [UQ_sys_team_code] UNIQUE ([code])
);

-- 人员维护表
CREATE TABLE smtBackend.sys_person
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [team_id]     BIGINT       NOT NULL, -- 组别ID
    [name]        NVARCHAR(255) NOT NULL, -- 人员姓名
    [employee_no] NVARCHAR(64)  NULL, -- 工号
    [remark]      NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_person_team] FOREIGN KEY ([team_id]) REFERENCES smtBackend.sys_team([id]),
    CONSTRAINT [UQ_sys_person_team_name] UNIQUE ([team_id], [name]),
    CONSTRAINT [UQ_sys_person_employee_no] UNIQUE ([employee_no])
);

-- 将维修记录外键切换到系统维护表
ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_workshop];
ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_line];
ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_machine];
ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_abnormal_type];
ALTER TABLE smtBackend.repair_record DROP CONSTRAINT [FK_repair_record_responsible_person];

ALTER TABLE smtBackend.repair_record
    ADD CONSTRAINT [FK_repair_record_workshop]
        FOREIGN KEY ([workshop_id], [factory_id]) REFERENCES smtBackend.sys_workshop([id], [factory_id]),
        CONSTRAINT [FK_repair_record_line]
        FOREIGN KEY ([line_id], [workshop_id]) REFERENCES smtBackend.sys_line([id], [workshop_id]),
        CONSTRAINT [FK_repair_record_machine]
        FOREIGN KEY ([machine_id], [model_id]) REFERENCES smtBackend.sys_machine([id], [model_id]),
        CONSTRAINT [FK_repair_record_abnormal_type]
        FOREIGN KEY ([abnormal_type_id], [abnormal_category_id])
            REFERENCES smtBackend.sys_abnormal_type([id], [abnormal_category_id]),
        CONSTRAINT [FK_repair_record_responsible_person]
        FOREIGN KEY ([responsible_person_id], [team_id]) REFERENCES smtBackend.sys_person([id], [team_id]);

ALTER TABLE smtBackend.repair_record_person DROP CONSTRAINT [FK_repair_record_person_person];

ALTER TABLE smtBackend.repair_record_person
    ADD CONSTRAINT [FK_repair_record_person_person]
        FOREIGN KEY ([person_id]) REFERENCES smtBackend.sys_person([id]);
