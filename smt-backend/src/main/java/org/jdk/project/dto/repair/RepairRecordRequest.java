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

  @NotBlank @Pattern(regexp = "(?i)DAY|NIGHT", message = "shift must be DAY or NIGHT")
  private String shift;

  @NotBlank private String factoryName;
  @NotBlank private String workshopName;
  @NotBlank private String lineName;
  @NotBlank private String modelName;
  @NotBlank private String machineNo;
  @NotBlank private String abnormalCategoryName;
  @NotBlank private String abnormalTypeName;
  @NotBlank private String abnormalDesc;
  private String solution;
  @NotNull private Boolean isFixed;
  private LocalDateTime fixedAt;
  @PositiveOrZero private Integer repairMinutes;
  @NotBlank private String teamName;
  @NotBlank private String responsiblePersonName;
  private List<String> repairPersonNames;
}
