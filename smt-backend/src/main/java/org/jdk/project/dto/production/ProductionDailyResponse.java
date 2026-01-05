package org.jdk.project.dto.production;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProductionDailyResponse {

  private Long headerId;

  private LocalDate prodDate;

  private String shift;

  private String factoryName;

  private String workshopName;

  private String lineName;

  private List<ProductionDailyProcessViewDto> processes;
}
