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
  private Long factoryId;
  private String factoryName;
  private Long workshopId;
  private String workshopName;
  private Long lineId;
  private String lineName;
  private Long modelId;
  private String modelName;
  private Long machineId;
  private String machineNo;
  private Long abnormalCategoryId;
  private String abnormalCategoryName;
  private Long abnormalTypeId;
  private String abnormalTypeName;
  private String abnormalDesc;
  private String solution;
  private Boolean isFixed;
  private LocalDateTime fixedAt;
  private Integer repairMinutes;
  private Long teamId;
  private String teamName;
  private Long responsiblePersonId;
  private String responsiblePersonName;
  private List<Long> repairPersonIds;
}
