package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 通用维护表请求（名称/编码/排序/启用/备注）。 */
@Data
public class SysSimpleRequest {
  @NotBlank private String name;
  private String code;
  private Integer sortOrder;
  private Boolean isActive;
  private String remark;
}
