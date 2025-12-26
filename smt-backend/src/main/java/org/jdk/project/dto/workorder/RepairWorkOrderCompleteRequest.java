package org.jdk.project.dto.workorder;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairWorkOrderCompleteRequest {

  @NotBlank private String ca;
}
