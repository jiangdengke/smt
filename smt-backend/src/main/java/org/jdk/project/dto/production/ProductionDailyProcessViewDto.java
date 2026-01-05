package org.jdk.project.dto.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ProductionDailyProcessViewDto {

  private Long id;

  private Long headerId;

  private LocalDate prodDate;

  private String shift;

  private String factoryName;

  private String workshopName;

  private String lineName;

  private String machineNo;

  private String processName;

  private String productCode;

  private String seriesName;

  private BigDecimal ct;

  private Integer equipmentCount;

  private Integer runMinutes;

  private Integer targetOutput;

  private Integer actualOutput;

  private Integer gap;

  private BigDecimal achievementRate;

  private Integer downMinutes;

  private String fa;

  private String ca;
}
