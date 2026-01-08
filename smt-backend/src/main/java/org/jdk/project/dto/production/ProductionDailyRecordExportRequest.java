package org.jdk.project.dto.production;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ProductionDailyRecordExportRequest {

  @NotEmpty private List<Long> ids;
}
