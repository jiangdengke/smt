-- 维修记录相关表

-- 厂区表
CREATE TABLE smtBackend.factory
(
    [id]   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name] NVARCHAR(255) NOT NULL, -- 厂区名称
    CONSTRAINT [PK_factory] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_factory_name] UNIQUE ([name])
);

-- 车间表
CREATE TABLE smtBackend.workshop
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [factory_id] BIGINT       NOT NULL, -- 厂区ID
    [name]       NVARCHAR(255) NOT NULL, -- 车间名称
    CONSTRAINT [PK_workshop] PRIMARY KEY ([id]),
    CONSTRAINT [FK_workshop_factory] FOREIGN KEY ([factory_id]) REFERENCES smtBackend.factory([id]),
    CONSTRAINT [UQ_workshop_factory_name] UNIQUE ([factory_id], [name]),
    CONSTRAINT [UQ_workshop_id_factory] UNIQUE ([id], [factory_id])
);

-- 线别表
CREATE TABLE smtBackend.line
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [workshop_id] BIGINT       NOT NULL, -- 车间ID
    [name]        NVARCHAR(255) NOT NULL, -- 线别名称
    CONSTRAINT [PK_line] PRIMARY KEY ([id]),
    CONSTRAINT [FK_line_workshop] FOREIGN KEY ([workshop_id]) REFERENCES smtBackend.workshop([id]),
    CONSTRAINT [UQ_line_workshop_name] UNIQUE ([workshop_id], [name]),
    CONSTRAINT [UQ_line_id_workshop] UNIQUE ([id], [workshop_id])
);

-- 机型表
CREATE TABLE smtBackend.model
(
    [id]   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name] NVARCHAR(255) NOT NULL, -- 机型名称
    CONSTRAINT [PK_model] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_model_name] UNIQUE ([name])
);

-- 机台表
CREATE TABLE smtBackend.machine
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [model_id]   BIGINT       NOT NULL, -- 机型ID
    [machine_no] NVARCHAR(64) NOT NULL, -- 机台号
    CONSTRAINT [PK_machine] PRIMARY KEY ([id]),
    CONSTRAINT [FK_machine_model] FOREIGN KEY ([model_id]) REFERENCES smtBackend.model([id]),
    CONSTRAINT [UQ_machine_model_no] UNIQUE ([model_id], [machine_no]),
    CONSTRAINT [UQ_machine_id_model] UNIQUE ([id], [model_id])
);

-- 异常类别表
CREATE TABLE smtBackend.abnormal_category
(
    [id]   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name] NVARCHAR(255) NOT NULL, -- 异常类别名称
    CONSTRAINT [PK_abnormal_category] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_abnormal_category_name] UNIQUE ([name])
);

-- 异常分类表
CREATE TABLE smtBackend.abnormal_type
(
    [id]                   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [abnormal_category_id] BIGINT       NOT NULL, -- 异常类别ID
    [name]                 NVARCHAR(255) NOT NULL, -- 异常分类名称
    CONSTRAINT [PK_abnormal_type] PRIMARY KEY ([id]),
    CONSTRAINT [FK_abnormal_type_category]
        FOREIGN KEY ([abnormal_category_id]) REFERENCES smtBackend.abnormal_category([id]),
    CONSTRAINT [UQ_abnormal_type_category_name] UNIQUE ([abnormal_category_id], [name]),
    CONSTRAINT [UQ_abnormal_type_id_category] UNIQUE ([id], [abnormal_category_id])
);

-- 组别表
CREATE TABLE smtBackend.team
(
    [id]   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name] NVARCHAR(255) NOT NULL, -- 组别名称
    CONSTRAINT [PK_team] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_team_name] UNIQUE ([name])
);

-- 人员表
CREATE TABLE smtBackend.person
(
    [id]      BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [team_id] BIGINT       NOT NULL, -- 组别ID
    [name]    NVARCHAR(255) NOT NULL, -- 人员姓名
    CONSTRAINT [PK_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_person_team] FOREIGN KEY ([team_id]) REFERENCES smtBackend.team([id]),
    CONSTRAINT [UQ_person_team_name] UNIQUE ([team_id], [name]),
    CONSTRAINT [UQ_person_id_team] UNIQUE ([id], [team_id])
);

-- 维修记录表
CREATE TABLE smtBackend.repair_record
(
    [id]                    BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [occur_at]              DATETIME2     NOT NULL, -- 异常发生时间
    [shift]                 NVARCHAR(8)   NOT NULL, -- 班次(白/夜)
    [factory_id]            BIGINT        NOT NULL, -- 厂区ID
    [workshop_id]           BIGINT        NOT NULL, -- 车间ID
    [line_id]               BIGINT        NOT NULL, -- 线别ID
    [model_id]              BIGINT        NOT NULL, -- 机型ID
    [machine_id]            BIGINT        NOT NULL, -- 机台ID
    [abnormal_category_id]  BIGINT        NOT NULL, -- 异常类别ID
    [abnormal_type_id]      BIGINT        NOT NULL, -- 异常分类ID
    [abnormal_desc]         NVARCHAR(2000) NOT NULL, -- 异常描述
    [solution]              NVARCHAR(2000) NULL, -- 解决对策
    [is_fixed]              BIT           NOT NULL DEFAULT 0, -- 是否已修复
    [fixed_at]              DATETIME2     NULL, -- 修复时间
    [repair_minutes]        INT           NULL, -- 维修耗时(分钟)
    [team_id]               BIGINT        NOT NULL, -- 维修组别ID
    [responsible_person_id] BIGINT        NOT NULL, -- 责任人ID
    CONSTRAINT [PK_repair_record] PRIMARY KEY ([id]),
    CONSTRAINT [CK_repair_record_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [CK_repair_record_fixed_at]
        CHECK (([is_fixed] = 0 AND [fixed_at] IS NULL) OR ([is_fixed] = 1 AND [fixed_at] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes]
        CHECK (([is_fixed] = 0 AND [repair_minutes] IS NULL) OR ([is_fixed] = 1 AND [repair_minutes] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes_nonnegative]
        CHECK ([repair_minutes] IS NULL OR [repair_minutes] >= 0),
    CONSTRAINT [FK_repair_record_workshop]
        FOREIGN KEY ([workshop_id], [factory_id]) REFERENCES smtBackend.workshop([id], [factory_id]),
    CONSTRAINT [FK_repair_record_line]
        FOREIGN KEY ([line_id], [workshop_id]) REFERENCES smtBackend.line([id], [workshop_id]),
    CONSTRAINT [FK_repair_record_machine]
        FOREIGN KEY ([machine_id], [model_id]) REFERENCES smtBackend.machine([id], [model_id]),
    CONSTRAINT [FK_repair_record_abnormal_type]
        FOREIGN KEY ([abnormal_type_id], [abnormal_category_id])
            REFERENCES smtBackend.abnormal_type([id], [abnormal_category_id]),
    CONSTRAINT [FK_repair_record_responsible_person]
        FOREIGN KEY ([responsible_person_id], [team_id]) REFERENCES smtBackend.person([id], [team_id])
);

-- 维修记录-人员关系表
CREATE TABLE smtBackend.repair_record_person
(
    [repair_record_id] BIGINT NOT NULL, -- 维修记录ID
    [person_id]        BIGINT NOT NULL, -- 维修人员ID
    CONSTRAINT [PK_repair_record_person] PRIMARY KEY ([repair_record_id], [person_id]),
    CONSTRAINT [FK_repair_record_person_record]
        FOREIGN KEY ([repair_record_id]) REFERENCES smtBackend.repair_record([id]),
    CONSTRAINT [FK_repair_record_person_person]
        FOREIGN KEY ([person_id]) REFERENCES smtBackend.person([id])
);
