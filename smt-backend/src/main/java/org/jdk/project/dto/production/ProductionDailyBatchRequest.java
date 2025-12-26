package org.jdk.project.dto.production;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProductionDailyBatchRequest {

  @NotNull private LocalDate prodDate;

  @NotNull private String shift;

  @NotEmpty @Valid private List<ProductionDailyProcessRequest> processes;
}
