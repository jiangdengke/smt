package org.jdk.project.dto.workorder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RepairWorkOrderViewDto {

  private Long id;

  private Long sourceProcessId;

  private LocalDate prodDate;

  private String shift;

  private String processName;

  private String productCode;

  private String seriesName;

  private String fa;

  private String status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
