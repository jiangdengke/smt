package org.jdk.project.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新增账号请求。 */
@Data
public class UserCreateRequest {
  @NotBlank private String username;
  @NotBlank private String password;
  @NotBlank private String roleCode;
}
