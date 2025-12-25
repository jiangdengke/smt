package org.jdk.project.dto.repair;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** 维修记录查询条件。 */
@Data
public class RepairRecordQueryDto {
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime occurFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime occurTo;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fixedFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fixedTo;

  private String shift;
  private String factoryName;
  private String workshopName;
  private String lineName;
  private String modelName;
  private String machineNo;
  private String abnormalCategoryName;
  private String abnormalTypeName;
  private Boolean isFixed;
  private String teamName;
  private String responsiblePersonName;
  private String repairPersonName;
}
