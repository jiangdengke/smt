package org.jdk.project.dto.sign;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/** 登录请求 DTO。 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SignInDto {

  @NotEmpty private String username;

  @NotEmpty private String password;
}
