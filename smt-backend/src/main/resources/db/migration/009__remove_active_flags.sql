-- 移除启用状态字段（账号/维护表）

DECLARE @constraint NVARCHAR(200);

-- 用户启用字段
SELECT @constraint = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
JOIN sys.tables t ON t.object_id = c.object_id
JOIN sys.schemas s ON s.schema_id = t.schema_id
WHERE s.name = 'smtBackend' AND t.name = 'user' AND c.name = 'enable';
IF @constraint IS NOT NULL
    EXEC('ALTER TABLE smtBackend.[user] DROP CONSTRAINT [' + @constraint + ']');
IF COL_LENGTH('smtBackend.[user]', 'enable') IS NOT NULL
    ALTER TABLE smtBackend.[user] DROP COLUMN [enable];

-- sys_* 维护表启用字段
DECLARE @table NVARCHAR(128);
DECLARE @sql NVARCHAR(MAX);

DECLARE cur CURSOR LOCAL FAST_FORWARD FOR
SELECT t.name
FROM sys.tables t
JOIN sys.schemas s ON s.schema_id = t.schema_id
WHERE s.name = 'smtBackend'
  AND t.name IN (
    'sys_factory', 'sys_workshop', 'sys_line', 'sys_model', 'sys_machine',
    'sys_abnormal_category', 'sys_abnormal_type', 'sys_team', 'sys_person'
  );

OPEN cur;
FETCH NEXT FROM cur INTO @table;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @constraint = NULL;
    SELECT @constraint = dc.name
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.default_object_id = dc.object_id
    JOIN sys.tables t ON t.object_id = c.object_id
    JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = 'smtBackend' AND t.name = @table AND c.name = 'is_active';
    IF @constraint IS NOT NULL
    BEGIN
        SET @sql = 'ALTER TABLE smtBackend.' + QUOTENAME(@table) + ' DROP CONSTRAINT ' + QUOTENAME(@constraint);
        EXEC(@sql);
    END
    IF COL_LENGTH('smtBackend.' + @table, 'is_active') IS NOT NULL
    BEGIN
        SET @sql = 'ALTER TABLE smtBackend.' + QUOTENAME(@table) + ' DROP COLUMN [is_active]';
        EXEC(@sql);
    END
    FETCH NEXT FROM cur INTO @table;
END
CLOSE cur;
DEALLOCATE cur;
