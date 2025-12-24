package org.jdk.project.dto.sign;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/** 注册请求 DTO。 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {
  @NotEmpty private String username;

  @NotEmpty private String password;
}
