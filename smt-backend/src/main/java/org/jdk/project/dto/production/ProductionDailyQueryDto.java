package org.jdk.project.dto.production;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ProductionDailyQueryDto {

  private LocalDate from;

  private LocalDate to;

  private String shift;

  private String factoryName;

  private String workshopName;

  private String lineName;
}
