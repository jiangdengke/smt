package org.jdk.project.dto.repair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 维修记录新增/更新请求。 */
@Data
public class RepairRecordRequest {
  @NotNull private LocalDateTime occurAt;

  @NotBlank
  @Pattern(regexp = "(?i)DAY|NIGHT", message = "shift must be DAY or NIGHT")
  private String shift;

  @NotNull private Long factoryId;
  @NotNull private Long workshopId;
  @NotNull private Long lineId;
  @NotNull private Long modelId;
  @NotNull private Long machineId;
  @NotNull private Long abnormalCategoryId;
  @NotNull private Long abnormalTypeId;
  @NotBlank private String abnormalDesc;
  private String solution;
  @NotNull private Boolean isFixed;
  private LocalDateTime fixedAt;
  @PositiveOrZero private Integer repairMinutes;
  @NotNull private Long teamId;
  @NotNull private Long responsiblePersonId;
  private List<Long> repairPersonIds;
}
