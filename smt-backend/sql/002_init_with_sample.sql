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
    [machine_no]            NVARCHAR(64)  NOT NULL, -- 机台号快照
    [source_process_id]     BIGINT        NULL, -- 来源制程段ID(生产日报)
    [abnormal_category_name] NVARCHAR(255) NULL, -- 异常类别名称快照
    [abnormal_type_name]    NVARCHAR(255) NULL, -- 异常分类名称快照
    [team_name]             NVARCHAR(255) NULL, -- 组别名称快照
    [responsible_person_name] NVARCHAR(255) NULL, -- 责任人名称快照
    [abnormal_desc]         NVARCHAR(2000) NOT NULL, -- 异常描述
    [solution]              NVARCHAR(2000) NULL, -- 解决对策
    [is_fixed]              BIT           NOT NULL DEFAULT 0, -- 是否已修复
    [fixed_at]              DATETIME2     NULL, -- 修复时间
    [repair_minutes]        INT           NULL, -- 维修耗时(分钟)
    [down_minutes]          INT           NULL, -- 理论Down机时间(分钟)
    CONSTRAINT [PK_repair_record] PRIMARY KEY ([id]),
    CONSTRAINT [CK_repair_record_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [CK_repair_record_fixed_at]
        CHECK (([is_fixed] = 0 AND [fixed_at] IS NULL) OR ([is_fixed] = 1 AND [fixed_at] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes]
        CHECK (([is_fixed] = 0 AND [repair_minutes] IS NULL) OR ([is_fixed] = 1 AND [repair_minutes] IS NOT NULL)),
    CONSTRAINT [CK_repair_record_minutes_nonnegative]
        CHECK ([repair_minutes] IS NULL OR [repair_minutes] >= 0),
    CONSTRAINT [CK_repair_record_down_minutes_nonnegative]
        CHECK ([down_minutes] IS NULL OR [down_minutes] >= 0)
);

CREATE INDEX [IX_repair_record_source_process]
    ON smtBackend.repair_record ([source_process_id]);

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

-- 每日产能表
CREATE TABLE smtBackend.production_daily_header
(
    [id]            BIGINT      NOT NULL IDENTITY(1,1), -- 主键
    [prod_date]     DATE        NOT NULL, -- 日期
    [shift]         NVARCHAR(8) NOT NULL, -- 班别(DAY/NIGHT)
    [factory_name]  NVARCHAR(255) NOT NULL, -- 厂区名称
    [workshop_name] NVARCHAR(255) NOT NULL, -- 车间名称
    [line_name]     NVARCHAR(255) NOT NULL, -- 线别名称
    CONSTRAINT [PK_production_daily_header] PRIMARY KEY ([id]),
    CONSTRAINT [CK_production_daily_header_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [UQ_production_daily_header_date_shift]
        UNIQUE ([prod_date], [shift], [factory_name], [workshop_name], [line_name])
);

CREATE TABLE smtBackend.production_daily_process
(
    [id]               BIGINT        NOT NULL IDENTITY(1,1), -- 主键
    [header_id]        BIGINT        NOT NULL, -- 班别头ID
    [machine_no]       NVARCHAR(64)  NOT NULL, -- 机台号
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
    CONSTRAINT [UQ_production_daily_process_header_key]
        UNIQUE ([header_id], [process_name], [machine_no])
);

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

CREATE TABLE smtBackend.sys_machine
(
    [id]         BIGINT       NOT NULL IDENTITY(1,1), -- 主键
    [line_id]    BIGINT       NOT NULL, -- 线别ID
    [machine_no] NVARCHAR(64) NOT NULL, -- 机台号
    [sort_order] INT          NOT NULL DEFAULT 0, -- 排序号
    [remark]     NVARCHAR(500) NULL, -- 备注
    CONSTRAINT [PK_sys_machine] PRIMARY KEY ([id]),
    CONSTRAINT [FK_sys_machine_line] FOREIGN KEY ([line_id]) REFERENCES smtBackend.sys_line([id]),
    CONSTRAINT [UQ_sys_machine_line_no] UNIQUE ([line_id], [machine_no])
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

INSERT INTO smtBackend.[role_permission] ([role_id], [permission_id])
SELECT r.[id], p.[id]
FROM smtBackend.[role] r
JOIN smtBackend.[permission] p
    ON (r.[code] = N'ADMIN' AND p.[code] IN (N'sys:write', N'repair:read', N'repair:write'))
    OR (r.[code] = N'USER' AND p.[code] = N'repair:read')
    OR (r.[code] = N'PRODUCTION' AND p.[code] = N'report:read');

INSERT INTO smtBackend.[user] ([username], [password])
VALUES (N'admin', N'123456'),
       (N'engineer', N'123456'),
       (N'production', N'123456');

INSERT INTO smtBackend.[user_role] ([user_id], [role_id])
SELECT u.[id], r.[id]
FROM smtBackend.[user] u
JOIN smtBackend.[role] r
    ON (u.[username] = N'admin' AND r.[code] = N'ADMIN')
    OR (u.[username] = N'engineer' AND r.[code] = N'USER')
    OR (u.[username] = N'production' AND r.[code] = N'PRODUCTION');

-- 示例数据：系统维护表
INSERT INTO smtBackend.sys_factory ([name], [code], [sort_order], [remark])
VALUES (N'A厂', N'FA', 1, N''),
       (N'B厂', N'FB', 2, N'');

INSERT INTO smtBackend.sys_workshop ([factory_id], [name], [code], [sort_order], [remark])
VALUES ((SELECT [id] FROM smtBackend.sys_factory WHERE [name] = N'A厂'), N'SMT-1', N'A-SMT1', 1, N''),
       ((SELECT [id] FROM smtBackend.sys_factory WHERE [name] = N'A厂'), N'SMT-2', N'A-SMT2', 2, N''),
       ((SELECT [id] FROM smtBackend.sys_factory WHERE [name] = N'B厂'), N'SMT-3', N'B-SMT3', 1, N'');

INSERT INTO smtBackend.sys_line ([workshop_id], [name], [code], [sort_order], [remark])
VALUES ((SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-1'), N'线别-1', N'L1', 1, N''),
       ((SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-1'), N'线别-2', N'L2', 2, N''),
       ((SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-2'), N'线别-3', N'L3', 1, N''),
       ((SELECT [id] FROM smtBackend.sys_workshop WHERE [name] = N'SMT-3'), N'线别-1', N'L1B', 1, N'');

INSERT INTO smtBackend.sys_machine ([line_id], [machine_no], [sort_order], [remark])
VALUES ((SELECT l.[id]
         FROM smtBackend.sys_line l
         JOIN smtBackend.sys_workshop w ON l.[workshop_id] = w.[id]
         WHERE w.[name] = N'SMT-1' AND l.[name] = N'线别-1'),
        N'M-102', 1, N''),
       ((SELECT l.[id]
         FROM smtBackend.sys_line l
         JOIN smtBackend.sys_workshop w ON l.[workshop_id] = w.[id]
         WHERE w.[name] = N'SMT-1' AND l.[name] = N'线别-1'),
        N'M-103', 2, N''),
       ((SELECT l.[id]
         FROM smtBackend.sys_line l
         JOIN smtBackend.sys_workshop w ON l.[workshop_id] = w.[id]
         WHERE w.[name] = N'SMT-2' AND l.[name] = N'线别-3'),
        N'F-218', 1, N''),
       ((SELECT l.[id]
         FROM smtBackend.sys_line l
         JOIN smtBackend.sys_workshop w ON l.[workshop_id] = w.[id]
         WHERE w.[name] = N'SMT-3' AND l.[name] = N'线别-1'),
        N'P-306', 1, N'');

INSERT INTO smtBackend.sys_abnormal_category ([name], [code], [sort_order], [remark])
VALUES (N'设备故障', N'DEV', 1, N''),
       (N'品质异常', N'QLT', 2, N''),
       (N'保养问题', N'MTN', 3, N'');

INSERT INTO smtBackend.sys_abnormal_type ([abnormal_category_id], [name], [code], [sort_order], [remark])
VALUES ((SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'设备故障'),
        N'送料异常', N'DEV-01', 1, N''),
       ((SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'设备故障'),
        N'抛料', N'DEV-02', 2, N''),
       ((SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'品质异常'),
        N'漏贴', N'QLT-01', 1, N''),
       ((SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'品质异常'),
        N'偏位', N'QLT-02', 2, N''),
       ((SELECT [id] FROM smtBackend.sys_abnormal_category WHERE [name] = N'保养问题'),
        N'润滑不足', N'MTN-01', 1, N'');

INSERT INTO smtBackend.sys_team ([name], [code], [sort_order], [remark])
VALUES (N'一组', N'T1', 1, N''),
       (N'二组', N'T2', 2, N''),
       (N'三组', N'T3', 3, N'');

INSERT INTO smtBackend.sys_person ([team_id], [name], [employee_no], [remark])
VALUES ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'一组'), N'李明', N'1001', N''),
       ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'一组'), N'王华', N'1002', N''),
       ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'二组'), N'周倩', N'2001', N''),
       ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'二组'), N'刘杰', N'2002', N''),
       ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'三组'), N'陈涛', N'3001', N''),
       ((SELECT [id] FROM smtBackend.sys_team WHERE [name] = N'三组'), N'杨洁', N'3002', N'');

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes], [down_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T08:20:00'), N'DAY',
     N'A厂', N'SMT-1', N'线别-1', N'M-102',
     N'设备故障', N'送料异常', N'一组', N'李明',
     N'贴片机连续抛料，吸嘴检测报警。', N'更换供料器弹簧并校准真空值。',
     1, CONVERT(DATETIME2, '2025-05-01T09:00:00'), 40, 30);

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes], [down_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T11:35:00'), N'DAY',
     N'A厂', N'SMT-2', N'线别-3', N'F-218',
     N'品质异常', N'漏贴', N'二组', N'周倩',
     N'首件抽检漏贴率异常。', N'调整贴装压力，补充物料巡检。',
     0, NULL, NULL, 0);

INSERT INTO smtBackend.repair_record
    ([occur_at], [shift],
     [factory_name], [workshop_name], [line_name], [machine_no],
     [abnormal_category_name], [abnormal_type_name], [team_name], [responsible_person_name],
     [abnormal_desc], [solution],
     [is_fixed], [fixed_at], [repair_minutes], [down_minutes])
VALUES
    (CONVERT(DATETIME2, '2025-05-01T19:10:00'), N'NIGHT',
     N'B厂', N'SMT-3', N'线别-1', N'P-306',
     N'保养问题', N'润滑不足', N'三组', N'陈涛',
     N'导轨摩擦噪音增大。', N'补充润滑油，清洁导轨。',
     1, CONVERT(DATETIME2, '2025-05-01T20:05:00'), 55, 20);
INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
SELECT r.[id], N'李明'
FROM smtBackend.repair_record r
WHERE r.[occur_at] = CONVERT(DATETIME2, '2025-05-01T08:20:00')
  AND r.[machine_no] = N'M-102';

INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
SELECT r.[id], N'王华'
FROM smtBackend.repair_record r
WHERE r.[occur_at] = CONVERT(DATETIME2, '2025-05-01T08:20:00')
  AND r.[machine_no] = N'M-102';

INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
SELECT r.[id], N'周倩'
FROM smtBackend.repair_record r
WHERE r.[occur_at] = CONVERT(DATETIME2, '2025-05-01T11:35:00')
  AND r.[machine_no] = N'F-218';

INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
SELECT r.[id], N'陈涛'
FROM smtBackend.repair_record r
WHERE r.[occur_at] = CONVERT(DATETIME2, '2025-05-01T19:10:00')
  AND r.[machine_no] = N'P-306';

INSERT INTO smtBackend.repair_record_person ([repair_record_id], [person_name])
SELECT r.[id], N'杨洁'
FROM smtBackend.repair_record r
WHERE r.[occur_at] = CONVERT(DATETIME2, '2025-05-01T19:10:00')
  AND r.[machine_no] = N'P-306';

INSERT INTO smtBackend.production_daily_header
    ([prod_date], [shift], [factory_name], [workshop_name], [line_name])
VALUES (CONVERT(DATE, '2025-05-01'), N'DAY', N'A厂', N'SMT-1', N'线别-1');

INSERT INTO smtBackend.production_daily_header
    ([prod_date], [shift], [factory_name], [workshop_name], [line_name])
VALUES (CONVERT(DATE, '2025-05-01'), N'NIGHT', N'B厂', N'SMT-3', N'线别-1');

INSERT INTO smtBackend.production_daily_process
    ([header_id], [machine_no], [process_name], [product_code], [series_name],
     [ct], [equipment_count], [run_minutes], [target_output],
     [actual_output], [gap], [achievement_rate], [down_minutes],
     [fa], [ca])
VALUES
    ((SELECT [id] FROM smtBackend.production_daily_header
      WHERE [prod_date] = CONVERT(DATE, '2025-05-01') AND [shift] = N'DAY'),
     N'M-102',
     N'贴片', N'PN-1001', N'系列A',
     1.25, 4, 480, 1000,
     920, -80, 92.00, 30,
     N'抛料告警', NULL);

INSERT INTO smtBackend.production_daily_process
    ([header_id], [machine_no], [process_name], [product_code], [series_name],
     [ct], [equipment_count], [run_minutes], [target_output],
     [actual_output], [gap], [achievement_rate], [down_minutes],
     [fa], [ca])
VALUES
    ((SELECT [id] FROM smtBackend.production_daily_header
      WHERE [prod_date] = CONVERT(DATE, '2025-05-01') AND [shift] = N'NIGHT'),
     N'P-306',
     N'回流', N'PN-1002', N'系列B',
     1.10, 3, 450, 900,
     870, -30, 96.67, 20,
     NULL, NULL);
