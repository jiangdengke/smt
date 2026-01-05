package org.jdk.project.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductionDailyProcessRequest {

  private Long id;

  @NotBlank private String machineNo;

  @NotBlank private String processName;

  @NotBlank private String productCode;

  @NotBlank private String seriesName;

  @NotNull @DecimalMin("0") private BigDecimal ct;

  @NotNull @Min(0) private Integer equipmentCount;

  @NotNull @Min(0) private Integer runMinutes;

  @NotNull @Min(0) private Integer targetOutput;

  @NotNull @Min(0) private Integer actualOutput;

  @NotNull @Min(0) private Integer downMinutes;

  private String fa;

  private String ca;
}
