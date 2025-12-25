package org.jdk.project.dto.sign;

import java.util.List;
import lombok.Data;

/** 当前登录用户返回对象。 */
@Data
public class SignMeDto {
  private Long id;
  private String username;
  private List<String> roles;
  private List<String> permissions;
}
