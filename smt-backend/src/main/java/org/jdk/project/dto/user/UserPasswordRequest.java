package org.jdk.project.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 修改本人密码请求。 */
@Data
public class UserPasswordRequest {
  @NotBlank private String newPassword;
}
