package org.jdk.project.dto.sys;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 车间维护请求。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysWorkshopRequest extends SysSimpleRequest {
  @NotNull private Long factoryId;
}
