package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 人员维护请求。 */
@Data
public class SysPersonRequest {
  @NotNull private Long teamId;
  @NotBlank private String name;
  private String employeeNo;
  private Boolean isActive;
  private String remark;
}
