-- 维修系统初始化脚本（建表 + 示例数据）
-- 适用于 SQL Server；请先切换到目标数据库

-- 业务 Schema：用于隔离表与约束命名空间
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'smtBackend')
    EXEC('CREATE SCHEMA smtBackend');

-- 用户表：基础账号信息
CREATE TABLE smtBackend.[user]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [username]    NVARCHAR(255) NOT NULL UNIQUE, -- 登录名（唯一）
    [create_time] DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    [password]    NVARCHAR(255) NOT NULL, -- 密码明文（未加密）
    CONSTRAINT [PK_user] PRIMARY KEY ([id])
);

-- 角色表：角色编码与展示信息
CREATE TABLE smtBackend.[role]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [code]        NVARCHAR(64)  NOT NULL UNIQUE, -- 角色编码（如 USER/ADMIN）
    [name]        NVARCHAR(255) NOT NULL, -- 角色名称
    [description] NVARCHAR(255) NULL, -- 角色描述
    [create_time] DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    CONSTRAINT [PK_role] PRIMARY KEY ([id])
);

-- 权限表：细粒度权限点
CREATE TABLE smtBackend.[permission]
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键，自增
    [code]        NVARCHAR(128) NOT NULL UNIQUE, -- 权限编码（如 user:read）
    [name]        NVARCHAR(255) NOT NULL, -- 权限名称
    [description] NVARCHAR(255) NULL, -- 权限描述
    [create_time] DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间（UTC）
    CONSTRAINT [PK_permission] PRIMARY KEY ([id])
);

-- 用户-角色关联表：多对多
CREATE TABLE smtBackend.[user_role]
(
    [user_id] BIGINT NOT NULL, -- 用户ID
    [role_id] BIGINT NOT NULL, -- 角色ID
    CONSTRAINT [PK_user_role] PRIMARY KEY ([user_id], [role_id]),
    CONSTRAINT [FK_user_role_user] FOREIGN KEY ([user_id]) REFERENCES smtBackend.[user]([id]),
    CONSTRAINT [FK_user_role_role] FOREIGN KEY ([role_id]) REFERENCES smtBackend.[role]([id])
);

-- 角色-权限关联表：多对多
CREATE TABLE smtBackend.[role_permission]
(
    [role_id]       BIGINT NOT NULL, -- 角色ID
    [permission_id] BIGINT NOT NULL, -- 权限ID
    CONSTRAINT [PK_role_permission] PRIMARY KEY ([role_id], [permission_id]),
    CONSTRAINT [FK_role_permission_role] FOREIGN KEY ([role_id]) REFERENCES smtBackend.[role]([id]),
    CONSTRAINT [FK_role_permission_permission]
        FOREIGN KEY ([permission_id]) REFERENCES smtBackend.[permission]([id])
);

-- 维修记录相关表
-- 维修记录表
CREATE TABLE smtBackend.repair_record
(
    [id]                    BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [occur_at]              DATETIME2     NOT NULL, -- 异常发生时间
    [shift]                 NVARCHAR(8)   NOT NULL, -- 班次(DAY/NIGHT)
    [factory_name]          NVARCHAR(255) NOT NULL, -- 厂区名称快照
    [workshop_name]         NVARCHAR(255) NOT NULL, -- 车间名称快照
    [line_name]             NVARCHAR(255) NOT NULL, -- 线别名称快照
    [model_name]            NVARCHAR(255) NOT NULL, -- 机型名称快照
    [machine_no]            NVARCHAR(64)  NOT NULL, -- 机台号快照
    [abnormal_category_name] NVARCHAR(255) NOT NULL, -- 异常类别名称快照
    [abnormal_type_name]    NVARCHAR(255) NOT NULL, -- 异常分类名称快照
    [team_name]             NVARCHAR(255) NOT NULL, -- 组别名称快照
    [responsible_person_name] NVARCHAR(255) NOT NULL, -- 责任人名称快照
    [abnormal_desc]         NVARCHAR(2000) NOT NULL, -- 异常描述
    [solution]              NVARCHAR(2000) NULL, -- 解决对策
    [is_fixed]              BIT           NOT NULL DEFAULT 0, -- 是否已修复
    [fixed_at]              DATETIME2     NULL, -- 修复时间
    [repair_minutes]        INT           NULL, -- 维修耗时(分钟)
    CONSTRAINT [PK_repair_record] PRIMARY KEY ([id]),
    CONSTRAINT [CK_repair_record_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [CK_repair_record_fixed_at]
        CHECK (([is_fixed] = 0 AND [fixed_at] IS NULL) OR ([is_fixed] = 1 AND [fixed_at] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes]
        CHECK (([is_fixed] = 0 AND [repair_minutes] IS NULL) OR ([is_fixed] = 1 AND [repair_minutes] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes_nonnegative]
        CHECK ([repair_minutes] IS NULL OR [repair_minutes] >= 0)
);

-- 维修记录-人员关系表
CREATE TABLE smtBackend.repair_record_person
(
    [id]               BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [repair_record_id] BIGINT        NOT NULL, -- 维修记录ID
    [person_name]      NVARCHAR(255) NOT NULL, -- 维修人员名称快照
    CONSTRAINT [PK_repair_record_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_repair_record_person_record]
        FOREIGN KEY ([repair_record_id]) REFERENCES smtBackend.repair_record([id])
);

-- 每日产能与维修工单表
CREATE TABLE smtBackend.production_daily_header
(
    [id]        BIGINT      NOT NULL IDENTITY(1,1), -- 主键
    [prod_date] DATE        NOT NULL, -- 日期
    [shift]     NVARCHAR(8) NOT NULL, -- 班别(DAY/NIGHT)
    CONSTRAINT [PK_production_daily_header] PRIMARY KEY ([id]),
    CONSTRAINT [CK_production_daily_header_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [UQ_production_daily_header_date_shift] UNIQUE ([prod_date], [shift])
);

CREATE TABLE smtBackend.production_daily_process
(
    [id]               BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [header_id]        BIGINT        NOT NULL, -- 班别头ID
    [process_name]     NVARCHAR(128) NOT NULL, -- 制程段
    [product_code]     NVARCHAR(128) NOT NULL, -- 生产料号
    [series_name]      NVARCHAR(128) NOT NULL, -- 系列
    [ct]               DECIMAL(10,2) NOT NULL, -- CT(生产填写)
    [equipment_count]  INT           NOT NULL, -- 投入设备量
    [run_minutes]      INT           NOT NULL, -- 投产时间(MIN)
    [target_output]    INT           NOT NULL, -- 目标产能
    [actual_output]    INT           NOT NULL, -- 实际产出
    [gap]              INT           NULL, -- GAP(系统计算)
    [achievement_rate] DECIMAL(6,2)  NULL, -- 达成率%(系统计算)
    [down_minutes]     INT           NOT NULL, -- 理论Down机时间
    [fa]               NVARCHAR(2000) NULL, -- 异常描述
    [ca]               NVARCHAR(2000) NULL, -- 解决对策
    CONSTRAINT [PK_production_daily_process] PRIMARY KEY ([id]),
    CONSTRAINT [FK_production_daily_process_header]
        FOREIGN KEY ([header_id]) REFERENCES smtBackend.production_daily_header([id]),
    CONSTRAINT [UQ_production_daily_process_header_name] UNIQUE ([header_id], [process_name])
);

CREATE TABLE smtBackend.repair_work_order
(
    [id]                BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [source_process_id] BIGINT        NOT NULL, -- 来源制程段ID
    [prod_date]         DATE          NULL, -- 日期快照
    [shift]             NVARCHAR(8)   NULL, -- 班别快照
    [process_name]      NVARCHAR(128) NULL, -- 制程段快照
    [product_code]      NVARCHAR(128) NULL, -- 生产料号快照
    [series_name]       NVARCHAR(128) NULL, -- 系列快照
    [fa]                NVARCHAR(2000) NOT NULL, -- 异常描述
    [status]            NVARCHAR(16)  NOT NULL DEFAULT N'OPEN', -- 状态
    [created_at]        DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 创建时间
    [updated_at]        DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(), -- 更新时间
    CONSTRAINT [PK_repair_work_order] PRIMARY KEY ([id]),
    CONSTRAINT [CK_repair_work_order_status] CHECK ([status] IN ('OPEN', 'IN_PROGRESS', 'DONE'))
);

CREATE INDEX [IX_repair_work_order_status]
    ON smtBackend.repair_work_order ([status]);

CREATE INDEX [IX_repair_work_order_source_process]
    ON smtBackend.repair_work_order ([source_process_id]);

-- 系统维护主数据表（下拉选项）
CREATE TABLE smtBackend.sys_factory
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 厂区名称
    [code]       NVARCHAR(64)  NULL, -- 厂区编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_factory] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_factory_name] UNIQUE ([name])
);

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
    CONSTRAINT [UQ_sys_workshop_factory_name] UNIQUE ([factory_id], [name])
);

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
    CONSTRAINT [UQ_sys_line_workshop_name] UNIQUE ([workshop_id], [name])
);

CREATE TABLE smtBackend.sys_model
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 机型名称
    [code]       NVARCHAR(64)  NULL, -- 机型编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_model] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_model_name] UNIQUE ([name])
);

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

CREATE TABLE smtBackend.sys_abnormal_category
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 异常类别名称
    [code]       NVARCHAR(64)  NULL, -- 异常类别编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_abnormal_category] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_abnormal_category_name] UNIQUE ([name])
);

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
    CONSTRAINT [UQ_sys_abnormal_type_category_name] UNIQUE ([abnormal_category_id], [name])
);

CREATE TABLE smtBackend.sys_team
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [name]       NVARCHAR(255) NOT NULL, -- 组别名称
    [code]       NVARCHAR(64)  NULL, -- 组别编码
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_team] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_sys_team_name] UNIQUE ([name])
);

CREATE TABLE smtBackend.sys_person
(
    [id]          BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [team_id]     BIGINT       NOT NULL, -- 组别ID
    [name]        NVARCHAR(255) NOT NULL, -- 人员姓名
    [employee_no] NVARCHAR(64)  NULL, -- 工号
    [remark]      NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_person] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_person_team] FOREIGN KEY ([team_id]) REFERENCES smtBackend.sys_team([id]),
    CONSTRAINT [UQ_sys_person_team_name] UNIQUE ([team_id], [name])
);

-- 可空字段唯一索引（允许多个 NULL）
CREATE UNIQUE INDEX [UX_sys_factory_code]
    ON smtBackend.sys_factory ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_workshop_code]
    ON smtBackend.sys_workshop ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_line_code]
    ON smtBackend.sys_line ([code])
    WHERE [code] IS NOT NULL;

CREATE UNIQUE INDEX [UX_sys_model_code]
    ON smtBackend.sys_model ([code])
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

-- 示例数据：角色/权限/用户
INSERT INTO smtBackend.[role] ([code], [name], [description])
VALUES (N'ADMIN', N'管理员', N'系统管理员'),
       (N'USER', N'普通用户', N'默认角色'),
       (N'PRODUCTION', N'生产端', N'生产端角色');

INSERT INTO smtBackend.[permission] ([code], [name], [description])
VALUES (N'sys:write', N'系统维护', N'标准字段维护'),
       (N'repair:read', N'维修查看', N'维修记录查看'),
       (N'repair:write', N'维修维护', N'维修记录维护'),
       (N'report:read', N'报表查看', N'每日报表查看');

DECLARE @RoleAdmin BIGINT = (SELECT [id] FROM smtBackend.[role] WHERE [code] = N'ADMIN');
DECLARE @RoleUser BIGINT = (SELECT [id] FROM smtBackend.[role] WHERE [code] = N'USER');
DECLARE @RoleProduction BIGINT = (SELECT [id] FROM smtBackend.[role] WHERE [code] = N'PRODUCTION');
DECLARE @PermSysWrite BIGINT = (SELECT [id] FROM smtBackend.[permission] WHERE [code] = N'sys:write');
DECLARE @PermRepairRead BIGINT = (SELECT [id] FROM smtBackend.[permission] WHERE [code] = N'repair:read');
DECLARE @PermRepairWrite BIGINT = (SELECT [id] FROM smtBackend.[permission] WHERE [code] = N'repair:write');
DECLARE @PermReportRead BIGINT = (SELECT [id] FROM smtBackend.[permission] WHERE [code] = N'report:read');

INSERT INTO smtBackend.[role_permission] ([role_id], [permission_id])
VALUES (@RoleAdmin, @PermSysWrite),
       (@RoleAdmin, @PermRepairRead),
       (@RoleAdmin, @PermRepairWrite),
       (@RoleUser, @PermRepairRead),
       (@RoleProduction, @PermReportRead);

INSERT INTO smtBackend.[user] ([username], [password])
VALUES (N'admin', N'admin123'),
       (N'engineer', N'engineer123'),
       (N'production', N'production123');

DECLARE @AdminUserId BIGINT = (SELECT [id] FROM smtBackend.[user] WHERE [username] = N'admin');
DECLARE @EngineerUserId BIGINT = (SELECT [id] FROM smtBackend.[user] WHERE [username] = N'engineer');
DECLARE @ProductionUserId BIGINT = (SELECT [id] FROM smtBackend.[user] WHERE [username] = N'production');

INSERT INTO smtBackend.[user_role] ([user_id], [role_id])
VALUES (@AdminUserId, @RoleAdmin),
       (@EngineerUserId, @RoleUser),
       (@ProductionUserId, @RoleProduction);

-- 示例数据：系统维护表
INSERT INTO smtBackend.sys_factory ([name], [code], [sort_order], [remark])
VALUES (N'A厂', N'FA', 1, N''),
       (N'B厂', N'FB', 2, N'');

DECLARE @FactoryA BIGINT = (SELECT [id] FROM smtBackend.sys_factory WHERE [name] = N'A厂');
DECLARE @FactoryB BIGINT = (SELECT [id] FROM smtBackend.sys_factory WHERE [name] = N'B厂');

INSERT INTO smtBackend.sys_workshop ([factory_id], [name], [code], [sort_order], [remark])
VALUES (@FactoryA, N'SMT-1', N'A-SMT1', 1, N''),
       (@FactoryA, N'SMT-2', N'A-SMT2', 2, N''),
       (@FactoryB, N'SMT-3', N'B-SMT3', 1, N'');

DECLARE @WorkshopSmt1 BIGINT = (SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-1');
DECLARE @WorkshopSmt2 BIGINT = (SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-2');
DECLARE @WorkshopSmt3 BIGINT = (SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-3');

INSERT INTO smtBackend.sys_line ([workshop_id], [name], [code], [sort_order], [remark])
VALUES (@WorkshopSmt1, N'线别-1', N'L1', 1, N''),
       (@WorkshopSmt1, N'线别-2', N'L2', 2, N''),
       (@WorkshopSmt2, N'线别-3', N'L3', 1, N''),
       (@WorkshopSmt3, N'线别-1', N'L1B', 1, N'');

DECLARE @Line1Smt1 BIGINT = (SELECT [id] FROM smtBackend.sys_line WHERE [workshop_id] = @WorkshopSmt1 AND [name] = N'线别-1');
DECLARE @Line3Smt2 BIGINT = (SELECT [id] FROM smtBackend.sys_line WHERE [workshop_id] = @WorkshopSmt2 AND [name] = N'线别-3');
DECLARE @Line1Smt3 BIGINT = (SELECT [id] FROM smtBackend.sys_line WHERE [workshop_id] = @WorkshopSmt3 AND [name] = N'线别-1');

INSERT INTO smtBackend.sys_model ([name], [code], [sort_order], [remark])
VALUES (N'Yamaha ZL', N'YZL', 1, N''),
       (N'Fuji NX', N'FNX', 2, N''),
       (N'Panasonic AM', N'PAM', 3, N'');

DECLARE @ModelYamaha BIGINT = (SELECT [id] FROM smtBackend.sys_model WHERE [name] = N'Yamaha ZL');
DECLARE @ModelFuji BIGINT = (SELECT [id] FROM smtBackend.sys_model WHERE [name] = N'Fuji NX');
DECLARE @ModelPanasonic BIGINT = (SELECT [id] FROM smtBackend.sys_model WHERE [name] = N'Panasonic AM');

INSERT INTO smtBackend.sys_machine ([model_id], [machine_no], [sort_order], [remark])
VALUES (@ModelYamaha, N'M-102', 1, N''),
       (@ModelYamaha, N'M-103', 2, N''),
       (@ModelFuji, N'F-218', 1, N''),
       (@ModelPanasonic, N'P-306', 1, N'');

DECLARE @MachineM102 BIGINT = (SELECT [id] FROM smtBackend.sys_machine WHERE [machine_no] = N'M-102');
DECLARE @MachineF218 BIGINT = (SELECT [id] FROM smtBackend.sys_machine WHERE [machine_no] = N'F-218');
DECLARE @MachineP306 BIGINT = (SELECT [id] FROM smtBackend.sys_machine WHERE [machine_no] = N'P-306');

INSERT INTO smtBackend.sys_abnormal_category ([name], [code], [sort_order], [remark])
VALUES (N'设备故障', N'DEV', 1, N''),
       (N'品质异常', N'QLT', 2, N''),
       (N'保养问题', N'MTN', 3, N'');

DECLARE @CategoryDevice BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'设备故障');
DECLARE @CategoryQuality BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'品质异常');
DECLARE @CategoryMaintain BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'保养问题');

INSERT INTO smtBackend.sys_abnormal_type ([abnormal_category_id], [name], [code], [sort_order], [remark])
VALUES (@CategoryDevice, N'送料异常', N'DEV-01', 1, N''),
       (@CategoryDevice, N'抛料', N'DEV-02', 2, N''),
       (@CategoryQuality, N'漏贴', N'QLT-01', 1, N''),
       (@CategoryQuality, N'偏位', N'QLT-02', 2, N''),
       (@CategoryMaintain, N'润滑不足', N'MTN-01', 1, N'');

DECLARE @TypeFeed BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_type WHERE [name] = N'送料异常');
DECLARE @TypeMiss BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_type WHERE [name] = N'漏贴');
DECLARE @TypeLube BIGINT = (SELECT [id] FROM smtBackend.sys_abnormal_type WHERE [name] = N'润滑不足');

INSERT INTO smtBackend.sys_team ([name], [code], [sort_order], [remark])
VALUES (N'一组', N'T1', 1, N''),
       (N'二组', N'T2', 2, N''),
       (N'三组', N'T3', 3, N'');

DECLARE @Team1 BIGINT = (SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'一组');
DECLARE @Team2 BIGINT = (SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'二组');
DECLARE @Team3 BIGINT = (SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'三组');

INSERT INTO smtBackend.sys_person ([team_id], [name], [employee_no], [remark])
VALUES (@Team1, N'李明', N'1001', N''),
       (@Team1, N'王华', N'1002', N''),
       (@Team2, N'周倩', N'2001', N''),
       (@Team2, N'刘杰', N'2002', N''),
       (@Team3, N'陈涛', N'3001', N''),
       (@Team3, N'杨洁', N'3002', N'');

DECLARE @PersonLi BIGINT = (SELECT [id] FROM smtBackend.sys_person WHERE [name] = N'李明');
DECLARE @PersonWang BIGINT = (SELECT [id] FROM smtBackend.sys_person WHERE [name] = N'王华');
DECLARE @PersonZhou BIGINT = (SELECT [id] FROM smtBackend.sys_person WHERE [name] = N'周倩');
DECLARE @PersonChen BIGINT = (SELECT [id] FROM smtBackend.sys_person WHERE [name] = N'陈涛');
DECLARE @PersonYang BIGINT = (SELECT [id] FROM smtBackend.sys_person WHERE [name] = N'杨洁');

-- 示例数据：维修记录
DECLARE @Record1Id BIGINT;
DECLARE @Record2Id BIGINT;
DECLARE @Record3Id BIGINT;

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [model_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T08:20:00'), N'DAY',
     (SELECT [name] FROM smtBackend.sys_factory WHERE [id] = @FactoryA),
     (SELECT [name] FROM smtBackend.sys_workshop WHERE [id] = @WorkshopSmt1),
     (SELECT [name] FROM smtBackend.sys_line WHERE [id] = @Line1Smt1),
     (SELECT [name] FROM smtBackend.sys_model WHERE [id] = @ModelYamaha),
     (SELECT [machine_no] FROM smtBackend.sys_machine WHERE [id] = @MachineM102),
     (SELECT [name] FROM smtBackend.sys_abnormal_category WHERE [id] = @CategoryDevice),
     (SELECT [name] FROM smtBackend.sys_abnormal_type WHERE [id] = @TypeFeed),
     (SELECT [name] FROM smtBackend.sys_team WHERE [id] = @Team1),
     (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonLi),
     N'贴片机连续抛料，吸嘴检测报警。', N'更换供料器弹簧并校准真空值。',
     1, CONVERT(DATETIME2, '2025-05-01T09:00:00'), 40);
SET @Record1Id = SCOPE_IDENTITY();

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [model_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T11:35:00'), N'DAY',
     (SELECT [name] FROM smtBackend.sys_factory WHERE [id] = @FactoryA),
     (SELECT [name] FROM smtBackend.sys_workshop WHERE [id] = @WorkshopSmt2),
     (SELECT [name] FROM smtBackend.sys_line WHERE [id] = @Line3Smt2),
     (SELECT [name] FROM smtBackend.sys_model WHERE [id] = @ModelFuji),
     (SELECT [machine_no] FROM smtBackend.sys_machine WHERE [id] = @MachineF218),
     (SELECT [name] FROM smtBackend.sys_abnormal_category WHERE [id] = @CategoryQuality),
     (SELECT [name] FROM smtBackend.sys_abnormal_type WHERE [id] = @TypeMiss),
     (SELECT [name] FROM smtBackend.sys_team WHERE [id] = @Team2),
     (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonZhou),
     N'首件抽检漏贴率异常。', N'调整贴装压力，补充物料巡检。',
     0, NULL, NULL);
SET @Record2Id = SCOPE_IDENTITY();

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [model_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T19:10:00'), N'NIGHT',
     (SELECT [name] FROM smtBackend.sys_factory WHERE [id] = @FactoryB),
     (SELECT [name] FROM smtBackend.sys_workshop WHERE [id] = @WorkshopSmt3),
     (SELECT [name] FROM smtBackend.sys_line WHERE [id] = @Line1Smt3),
     (SELECT [name] FROM smtBackend.sys_model WHERE [id] = @ModelPanasonic),
     (SELECT [machine_no] FROM smtBackend.sys_machine WHERE [id] = @MachineP306),
     (SELECT [name] FROM smtBackend.sys_abnormal_category WHERE [id] = @CategoryMaintain),
     (SELECT [name] FROM smtBackend.sys_abnormal_type WHERE [id] = @TypeLube),
     (SELECT [name] FROM smtBackend.sys_team WHERE [id] = @Team3),
     (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonChen),
     N'导轨摩擦噪音增大。', N'补充润滑油，清洁导轨。',
     1, CONVERT(DATETIME2, '2025-05-01T20:05:00'), 55);
SET @Record3Id = SCOPE_IDENTITY();

INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
VALUES (@Record1Id, (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonLi)),
       (@Record1Id, (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonWang)),
       (@Record2Id, (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonZhou)),
       (@Record3Id, (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonChen)),
       (@Record3Id, (SELECT [name] FROM smtBackend.sys_person WHERE [id] = @PersonYang));

-- 示例数据：每日产能
DECLARE @ProdHeaderDay BIGINT;
DECLARE @ProdHeaderNight BIGINT;
DECLARE @ProdProcessDay BIGINT;

INSERT INTO smtBackend.production_daily_header ([prod_date], [shift])
VALUES (CONVERT(DATE, '2025-05-01'), N'DAY');
SET @ProdHeaderDay = SCOPE_IDENTITY();

INSERT INTO smtBackend.production_daily_header ([prod_date], [shift])
VALUES (CONVERT(DATE, '2025-05-01'), N'NIGHT');
SET @ProdHeaderNight = SCOPE_IDENTITY();

INSERT INTO smtBackend.production_daily_process
    ([header_id], [process_name], [product_code], [series_name],
     [ct], [equipment_count], [run_minutes], [target_output],
     [actual_output], [gap], [achievement_rate], [down_minutes],
     [fa], [ca])
VALUES
    (@ProdHeaderDay, N'贴片', N'PN-1001', N'系列A',
     1.25, 4, 480, 1000,
     920, -80, 92.00, 30,
     N'抛料告警', NULL);
SET @ProdProcessDay = SCOPE_IDENTITY();

INSERT INTO smtBackend.production_daily_process
    ([header_id], [process_name], [product_code], [series_name],
     [ct], [equipment_count], [run_minutes], [target_output],
     [actual_output], [gap], [achievement_rate], [down_minutes],
     [fa], [ca])
VALUES
    (@ProdHeaderNight, N'回流', N'PN-1002', N'系列B',
     1.10, 3, 450, 900,
     870, -30, 96.67, 20,
     NULL, NULL);

INSERT INTO smtBackend.repair_work_order
    ([source_process_id], [prod_date], [shift], [process_name], [product_code], [series_name],
     [fa], [status])
VALUES
    (@ProdProcessDay, CONVERT(DATE, '2025-05-01'), N'DAY', N'贴片', N'PN-1001', N'系列A',
     N'抛料告警', N'OPEN');
