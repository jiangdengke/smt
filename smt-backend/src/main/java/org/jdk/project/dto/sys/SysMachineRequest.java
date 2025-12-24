package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 机台维护请求。 */
@Data
public class SysMachineRequest {
  @NotNull private Long modelId;
  @NotBlank private String machineNo;
  private Integer sortOrder;
  private Boolean isActive;
  private String remark;
}
