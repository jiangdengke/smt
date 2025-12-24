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

  private String shift;
  private Long factoryId;
  private Long workshopId;
  private Long lineId;
  private Long modelId;
  private Long machineId;
  private Long abnormalCategoryId;
  private Long abnormalTypeId;
  private Boolean isFixed;
  private Long teamId;
  private Long responsiblePersonId;
}
