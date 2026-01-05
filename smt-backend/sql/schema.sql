-- 维修系统建表脚本（不含示例数据）
-- 适用于 SQL Server；请先切换到目标数据库

-- 业务 Schema：用于隔离表与约束命名空间
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'smtBackend')
    EXEC('CREATE SCHEMA smtBackend');

-- 说明
-- 1) 维修记录与维护表解耦，repair_record 仅保存名称快照，不设外键。
-- 2) 维护表用于下拉联动：厂区 → 车间 → 线别 → 机台号。
-- 3) 每日产能用于生产端记录，异常可同步生成维修记录。

-- 用户表：系统账号信息，用于登录与权限管理
CREATE TABLE smtBackend.[user]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [username]    NVARCHAR(255) NOT NULL UNIQUE, -- 登录名（唯一，用于登录）
    [create_time] DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    [password]    NVARCHAR(255) NOT NULL, -- 密码明文（未加密）
    CONSTRAINT [PK_user] PRIMARY KEY ([id])
);

-- 角色表：账号权限分组
CREATE TABLE smtBackend.[role]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [code]        NVARCHAR(64)  NOT NULL UNIQUE, -- 角色编码（唯一，如 USER/ADMIN）
    [name]        NVARCHAR(255) NOT NULL, -- 角色名称
    [description] NVARCHAR(255) NULL, -- 角色描述
    [create_time] DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    CONSTRAINT [PK_role] PRIMARY KEY ([id])
);

-- 权限表：细粒度权限点定义
CREATE TABLE smtBackend.[permission]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [code]        NVARCHAR(128) NOT NULL UNIQUE, -- 权限编码（唯一，如 user:read）
    [name]        NVARCHAR(255) NOT NULL, -- 权限名称
    [description] NVARCHAR(255) NULL, -- 权限描述
    [create_time] DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    CONSTRAINT [PK_permission] PRIMARY KEY ([id])
);

-- 用户-角色关联表：多对多，用于账号授权
CREATE TABLE smtBackend.[user_role]
(
    [user_id] BIGINT NOT NULL, -- 用户ID（关联 user.id）
    [role_id] BIGINT NOT NULL, -- 角色ID（关联 role.id）
    CONSTRAINT [PK_user_role] PRIMARY KEY ([user_id], [role_id]),
    CONSTRAINT [FK_user_role_user] FOREIGN KEY ([user_id]) REFERENCES smtBackend.[user]([id]),
    CONSTRAINT [FK_user_role_role] FOREIGN KEY ([role_id]) REFERENCES smtBackend.[role]([id])
);

-- 角色-权限关联表：多对多，用于角色授权
CREATE TABLE smtBackend.[role_permission]
(
    [role_id]       BIGINT NOT NULL, -- 角色ID（关联 role.id）
    [permission_id] BIGINT NOT NULL, -- 权限ID（关联 permission.id）
    CONSTRAINT [PK_role_permission] PRIMARY KEY ([role_id], [permission_id]),
    CONSTRAINT [FK_role_permission_role] FOREIGN KEY ([role_id]) REFERENCES smtBackend.[role]([id]),
    CONSTRAINT [FK_role_permission_permission]
        FOREIGN KEY ([permission_id]) REFERENCES smtBackend.[permission]([id])
);

-- 维修记录相关表
-- 维修记录表：保存完整快照，保证历史记录不受维护表变更影响
CREATE TABLE smtBackend.repair_record
(
    [id]                    BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [occur_at]              DATETIME2     NOT NULL, -- 异常发生时间
    [shift]                 NVARCHAR(8)   NOT NULL, -- 班次：DAY=白班，NIGHT=夜班
    [factory_name]          NVARCHAR(255) NOT NULL, -- 厂区名称快照（来源维护表）
    [workshop_name]         NVARCHAR(255) NOT NULL, -- 车间名称快照（来源维护表）
    [line_name]             NVARCHAR(255) NOT NULL, -- 线别名称快照（来源维护表）
    [machine_no]            NVARCHAR(64)  NOT NULL, -- 机台号快照（来源维护表）
    [source_process_id]     BIGINT       NULL, -- 来源制程段ID（production_daily_process.id）
    [abnormal_category_name] NVARCHAR(255) NULL, -- 异常类别名称快照（生产自动记录可为空）
    [abnormal_type_name]    NVARCHAR(255) NULL, -- 异常分类名称快照（生产自动记录可为空）
    [team_name]             NVARCHAR(255) NULL, -- 组别名称快照（生产自动记录可为空）
    [responsible_person_name] NVARCHAR(255) NULL, -- 责任人名称快照（生产自动记录可为空）
    [abnormal_desc]         NVARCHAR(2000) NOT NULL, -- 异常描述（必填）
    [solution]              NVARCHAR(2000) NULL, -- 解决对策（可为空）
    [is_fixed]              BIT           NOT NULL DEFAULT 0, -- 是否已修复：0未修复/1已修复
    [fixed_at]              DATETIME2     NULL, -- 修复时间（已修复必填）
    [repair_minutes]        INT           NULL, -- 维修耗时(分钟，已修复必填)
    CONSTRAINT [PK_repair_record] PRIMARY KEY ([id]),
    CONSTRAINT [CK_repair_record_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [CK_repair_record_fixed_at]
        CHECK (([is_fixed] = 0 AND [fixed_at] IS NULL) OR ([is_fixed] = 1 AND [fixed_at] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes]
        CHECK (([is_fixed] = 0 AND [repair_minutes] IS NULL) OR ([is_fixed] = 1 AND [repair_minutes] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes_nonnegative]
        CHECK ([repair_minutes] IS NULL OR [repair_minutes] >= 0)
);

CREATE INDEX [IX_repair_record_source_process]
    ON smtBackend.repair_record ([source_process_id]);

-- 维修记录-人员关系表：同一记录可对应多个维修人
CREATE TABLE smtBackend.repair_record_person
(
    [id]               BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [repair_record_id] BIGINT        NOT NULL, -- 维修记录ID
    [person_name]      NVARCHAR(255) NOT NULL, -- 维修人员名称快照
    CONSTRAINT [PK_repair_record_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_repair_record_person_record]
        FOREIGN KEY ([repair_record_id]) REFERENCES smtBackend.repair_record([id])
);

-- 每日产能表
-- 每日产能表头：同一日期+班别+厂区+车间+线别唯一
CREATE TABLE smtBackend.production_daily_header
(
    [id]            BIGINT      NOT NULL IDENTITY(1,1), -- 主键
    [prod_date]     DATE        NOT NULL, -- 产能日期
    [shift]         NVARCHAR(8) NOT NULL, -- 班别：DAY=白班，NIGHT=夜班
    [factory_name]  NVARCHAR(255) NOT NULL, -- 厂区名称
    [workshop_name] NVARCHAR(255) NOT NULL, -- 车间名称
    [line_name]     NVARCHAR(255) NOT NULL, -- 线别名称
    CONSTRAINT [PK_production_daily_header] PRIMARY KEY ([id]),
    CONSTRAINT [CK_production_daily_header_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [UQ_production_daily_header_key]
        UNIQUE ([prod_date], [shift], [factory_name], [workshop_name], [line_name])
);

-- 每日产能明细：一个表头对应多条制程段数据
CREATE TABLE smtBackend.production_daily_process
(
    [id]               BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [header_id]        BIGINT        NOT NULL, -- 表头ID（关联 production_daily_header）
    [machine_no]       NVARCHAR(64)  NOT NULL, -- 机台号（生产端填写）
    [process_name]     NVARCHAR(128) NOT NULL, -- 制程段名称
    [product_code]     NVARCHAR(128) NOT NULL, -- 生产料号
    [series_name]      NVARCHAR(128) NOT NULL, -- 系列/机种系列
    [ct]               DECIMAL(10,2) NOT NULL, -- CT(秒，小数)
    [equipment_count]  INT           NOT NULL, -- 投入设备量(台)
    [run_minutes]      INT           NOT NULL, -- 投产时间(分钟)
    [target_output]    INT           NOT NULL, -- 目标产能（生产端填写）
    [actual_output]    INT           NOT NULL, -- 实际产出
    [gap]              INT           NULL, -- GAP=实际产出-目标产能（系统计算）
    [achievement_rate] DECIMAL(6,2)  NULL, -- 达成率%(系统计算)
    [down_minutes]     INT           NOT NULL, -- 理论Down机时间(分钟)
    [fa]               NVARCHAR(2000) NULL, -- 异常描述(FA)
    [ca]               NVARCHAR(2000) NULL, -- 解决对策(CA，维修回填)
    CONSTRAINT [PK_production_daily_process] PRIMARY KEY ([id]),
    CONSTRAINT [FK_production_daily_process_header]
        FOREIGN KEY ([header_id]) REFERENCES smtBackend.production_daily_header([id]),
    CONSTRAINT [UQ_production_daily_process_key]
        UNIQUE ([header_id], [process_name], [machine_no])
);

-- 系统维护主数据表（下拉选项）
-- 区域组织：厂区 → 车间 → 线别 → 机台号
-- 厂区表：维护厂区基础信息
CREATE TABLE smtBackend.sys_factory
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 厂区名称
    [code]       NVARCHAR(64)  NULL, -- 厂区编码（可为空）
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_factory] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_factory_name] UNIQUE ([name])
);

-- 车间表：维护厂区下的车间
CREATE TABLE smtBackend.sys_workshop
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [factory_id] BIGINT       NOT NULL, -- 厂区ID（关联 sys_factory）
    [name]       NVARCHAR(255) NOT NULL, -- 车间名称
    [code]       NVARCHAR(64)  NULL, -- 车间编码（可为空）
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_workshop] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_workshop_factory] FOREIGN KEY ([factory_id]) REFERENCES smtBackend.sys_factory([id]),
    CONSTRAINT [UQ_sys_workshop_factory_name] UNIQUE ([factory_id], [name])
);

-- 线别表：维护车间下的线别
CREATE TABLE smtBackend.sys_line
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [workshop_id] BIGINT       NOT NULL, -- 车间ID（关联 sys_workshop）
    [name]        NVARCHAR(255) NOT NULL, -- 线别名称
    [code]        NVARCHAR(64)  NULL, -- 线别编码（可为空）
    [sort_order]  INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]      NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_line] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_line_workshop] FOREIGN KEY ([workshop_id]) REFERENCES smtBackend.sys_workshop([id]),
    CONSTRAINT [UQ_sys_line_workshop_name] UNIQUE ([workshop_id], [name])
);

-- 机台表：维护线别下的机台号
CREATE TABLE smtBackend.sys_machine
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [line_id]    BIGINT       NOT NULL, -- 线别ID（关联 sys_line）
    [machine_no] NVARCHAR(64) NOT NULL, -- 机台号
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_machine] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_machine_line] FOREIGN KEY ([line_id]) REFERENCES smtBackend.sys_line([id]),
    CONSTRAINT [UQ_sys_machine_line_no] UNIQUE ([line_id], [machine_no])
);

-- 异常体系：类别 → 分类
-- 异常类别表：异常大类
CREATE TABLE smtBackend.sys_abnormal_category
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 异常类别名称
    [code]       NVARCHAR(64)  NULL, -- 异常类别编码（可为空）
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_abnormal_category] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_abnormal_category_name] UNIQUE ([name])
);

-- 异常分类表：隶属异常类别
CREATE TABLE smtBackend.sys_abnormal_type
(
    [id]                   BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [abnormal_category_id] BIGINT       NOT NULL, -- 异常类别ID（关联 sys_abnormal_category）
    [name]                 NVARCHAR(255) NOT NULL, -- 异常分类名称
    [code]                 NVARCHAR(64)  NULL, -- 异常分类编码（可为空）
    [sort_order]           INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]               NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_abnormal_type] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_abnormal_type_category]
        FOREIGN KEY ([abnormal_category_id]) REFERENCES smtBackend.sys_abnormal_category([id]),
    CONSTRAINT [UQ_sys_abnormal_type_category_name] UNIQUE ([abnormal_category_id], [name])
);

-- 组织与人员：组别 → 人员
-- 组别表：维修/人员组织
CREATE TABLE smtBackend.sys_team
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 组别名称
    [code]       NVARCHAR(64)  NULL, -- 组别编码（可为空）
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号（小在前）
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_team] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_team_name] UNIQUE ([name])
);

-- 人员表：隶属组别
CREATE TABLE smtBackend.sys_person
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [team_id]     BIGINT       NOT NULL, -- 组别ID（关联 sys_team）
    [name]        NVARCHAR(255) NOT NULL, -- 人员姓名
    [employee_no] NVARCHAR(64)  NULL, -- 工号（可为空）
    [remark]      NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_person_team] FOREIGN KEY ([team_id]) REFERENCES smtBackend.sys_team([id]),
    CONSTRAINT [UQ_sys_person_team_name] UNIQUE ([team_id], [name])
);

-- 可空字段唯一索引（允许多个 NULL）
-- 编码唯一索引：当 code 不为空时保证唯一性
CREATE UNIQUE INDEX [UX_sys_factory_code]
    ON smtBackend.sys_factory ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_workshop_code]
    ON smtBackend.sys_workshop ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_line_code]
    ON smtBackend.sys_line ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_abnormal_category_code]
    ON smtBackend.sys_abnormal_category ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_abnormal_type_code]
    ON smtBackend.sys_abnormal_type ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_team_code]
    ON smtBackend.sys_team ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_person_employee_no]
    ON smtBackend.sys_person ([employee_no])
    WHERE [employee_no] IS NOT NULL;
