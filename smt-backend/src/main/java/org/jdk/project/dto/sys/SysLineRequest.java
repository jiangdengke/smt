package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 线别维护请求。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLineRequest extends SysSimpleRequest {
  @NotNull private Long workshopId;
}
