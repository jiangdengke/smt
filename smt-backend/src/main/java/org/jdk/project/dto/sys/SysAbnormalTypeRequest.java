package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 异常分类维护请求。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAbnormalTypeRequest extends SysSimpleRequest {
  @NotNull private Long abnormalCategoryId;
}
