package org.jdk.project.dto.user;

import lombok.Data;

/** 账号修改请求（密码/角色）。 */
@Data
public class UserUpdateRequest {
  private String username;
  private String password;
  private String roleCode;
}
