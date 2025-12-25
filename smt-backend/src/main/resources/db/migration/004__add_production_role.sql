-- 新增生产端角色与日报权限

IF NOT EXISTS (SELECT 1 FROM smtBackend.[role] WHERE [code] = N'PRODUCTION')
    INSERT INTO smtBackend.[role] ([code], [name], [description])
    VALUES (N'PRODUCTION', N'生产端', N'生产端角色');

IF NOT EXISTS (SELECT 1 FROM smtBackend.[permission] WHERE [code] = N'report:read')
    INSERT INTO smtBackend.[permission] ([code], [name], [description])
    VALUES (N'report:read', N'报表查看', N'每日报表查看');

DECLARE @ProductionRoleId BIGINT = (SELECT [id] FROM smtBackend.[role] WHERE [code] = N'PRODUCTION');
DECLARE @ReportReadId BIGINT = (SELECT [id] FROM smtBackend.[permission] WHERE [code] = N'report:read');

IF NOT EXISTS (
    SELECT 1 FROM smtBackend.[role_permission]
    WHERE [role_id] = @ProductionRoleId AND [permission_id] = @ReportReadId
)
    INSERT INTO smtBackend.[role_permission] ([role_id], [permission_id])
    VALUES (@ProductionRoleId, @ReportReadId);
