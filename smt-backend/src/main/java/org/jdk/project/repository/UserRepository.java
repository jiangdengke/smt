package org.jdk.project.repository;

import java.util.List;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.generated.tables.Permission;
import org.jooq.generated.tables.Role;
import org.jooq.generated.tables.RolePermission;
import org.jooq.generated.tables.UserRole;
import org.jooq.generated.tables.daos.UserDao;
import org.jooq.generated.tables.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** 用户仓储：基于 jOOQ 的数据访问（精简版，仅保留必要方法）。 */
@Repository
public class UserRepository extends UserDao {

  private static final org.jooq.generated.tables.User USER = org.jooq.generated.tables.User.USER;
  private static final Role ROLE = Role.ROLE;
  private static final Permission PERMISSION = Permission.PERMISSION;
  private static final UserRole USER_ROLE = UserRole.USER_ROLE;
  private static final RolePermission ROLE_PERMISSION = RolePermission.ROLE_PERMISSION;

  @Autowired
  public UserRepository(Configuration configuration) {
    super(configuration);
  }

  /** 根据用户名删除用户。 */
  @Transactional
  public void deleteUserBy(String username) {
    ctx().delete(USER).where(USER.USERNAME.eq(username)).execute();
  }

  /** 根据用户名查询用户。 */
  public User fetchOneByUsername(String username) {
    return ctx()
        .selectFrom(USER)
        .where(USER.USERNAME.eq(username))
        .fetchOneInto(User.class);
  }

  /** 根据用户ID查询角色编码列表。 */
  public List<String> fetchRoleCodesByUserId(Long userId) {
    Field<Long> roleId = ROLE.ID.cast(Long.class);
    return ctx().select(ROLE.CODE)
        .from(USER_ROLE)
        .join(ROLE)
        .on(USER_ROLE.ROLE_ID.eq(roleId))
        .where(USER_ROLE.USER_ID.eq(userId))
        .fetch(ROLE.CODE);
  }

  /** 根据用户ID查询权限编码列表。 */
  public List<String> fetchPermissionCodesByUserId(Long userId) {
    Field<Long> permissionId = PERMISSION.ID.cast(Long.class);
    return ctx().selectDistinct(PERMISSION.CODE)
        .from(USER_ROLE)
        .join(ROLE_PERMISSION)
        .on(USER_ROLE.ROLE_ID.eq(ROLE_PERMISSION.ROLE_ID))
        .join(PERMISSION)
        .on(ROLE_PERMISSION.PERMISSION_ID.eq(permissionId))
        .where(USER_ROLE.USER_ID.eq(userId))
        .fetch(PERMISSION.CODE);
  }

  /** 根据角色编码获取角色ID。 */
  public Long fetchRoleIdByCode(String code) {
    Field<Long> roleId = ROLE.ID.cast(Long.class);
    return ctx().select(roleId).from(ROLE).where(ROLE.CODE.eq(code)).fetchOne(roleId);
  }

  /** 绑定用户与角色。 */
  @Transactional
  public void insertUserRole(Long userId, Long roleId) {
    ctx().insertInto(USER_ROLE)
        .columns(USER_ROLE.USER_ID, USER_ROLE.ROLE_ID)
        .values(userId, roleId)
        .execute();
  }
}
