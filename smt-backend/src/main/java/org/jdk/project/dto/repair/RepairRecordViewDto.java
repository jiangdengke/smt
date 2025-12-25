package org.jdk.project.dto.repair;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 维修记录展示 DTO（包含名称字段）。 */
@Data
public class RepairRecordViewDto {
  private Long id;
  private LocalDateTime occurAt;
  private String shift;
  private String factoryName;
  private String workshopName;
  private String lineName;
  private String modelName;
  private String machineNo;
  private String abnormalCategoryName;
  private String abnormalTypeName;
  private String abnormalDesc;
  private String solution;
  private Boolean isFixed;
  private LocalDateTime fixedAt;
  private Integer repairMinutes;
  private String teamName;
  private String responsiblePersonName;
  private List<String> repairPersonNames;
}
