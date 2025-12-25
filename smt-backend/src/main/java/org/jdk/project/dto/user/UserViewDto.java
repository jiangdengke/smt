package org.jdk.project.dto.user;

import lombok.Data;

/** 账号展示 DTO。 */
@Data
public class UserViewDto {
  private Long id;
  private String username;
  private String roleCode;
}
