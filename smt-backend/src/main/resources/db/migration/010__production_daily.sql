-- 每日产能与维修工单表

-- 班别头表：日期 + 班别
CREATE TABLE smtBackend.production_daily_header
(
    [id]        BIGINT      NOT NULL IDENTITY(1,1), -- 主键
    [prod_date] DATE        NOT NULL, -- 日期
    [shift]     NVARCHAR(8) NOT NULL, -- 班别(DAY/NIGHT)
    CONSTRAINT [PK_production_daily_header] PRIMARY KEY ([id]),
    CONSTRAINT [CK_production_daily_header_shift] CHECK ([shift] IN ('DAY', 'NIGHT')),
    CONSTRAINT [UQ_production_daily_header_date_shift] UNIQUE ([prod_date], [shift])
);

-- 制程段明细表：一条制程段对应一条明细
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

-- 维修工单：由生产端异常描述触发
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
