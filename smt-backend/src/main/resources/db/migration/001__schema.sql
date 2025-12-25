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
