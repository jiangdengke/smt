package org.jdk.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.workorder.RepairWorkOrderCompleteRequest;
import org.jdk.project.dto.workorder.RepairWorkOrderViewDto;
import org.jdk.project.service.RepairWorkOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 维修工单接口。 */
@RestController
@RequestMapping("/repair-work-orders")
@RequiredArgsConstructor
public class RepairWorkOrderController {

  private final RepairWorkOrderService repairWorkOrderService;

  @GetMapping
  @PreAuthorize("hasAuthority('repair:read')")
  public List<RepairWorkOrderViewDto> list(@RequestParam(required = false) String status) {
    return repairWorkOrderService.list(status);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/complete")
  @PreAuthorize("hasAuthority('repair:write') or hasAuthority('repair:read')")
  public void complete(
      @PathVariable Long id, @RequestBody @Valid RepairWorkOrderCompleteRequest request) {
    repairWorkOrderService.complete(id, request);
  }
}
