package org.jdk.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.production.ProductionDailyBatchRequest;
import org.jdk.project.dto.production.ProductionDailyQueryDto;
import org.jdk.project.dto.production.ProductionDailyResponse;
import org.jdk.project.service.ProductionDailyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 每日产能接口（生产端）。 */
@RestController
@RequestMapping("/production-daily")
@RequiredArgsConstructor
public class ProductionDailyController {

  private final ProductionDailyService productionDailyService;

  @GetMapping
  @PreAuthorize("hasRole('PRODUCTION')")
  public ProductionDailyResponse get(@RequestParam LocalDate prodDate, @RequestParam String shift) {
    return productionDailyService.get(prodDate, shift);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/batch")
  @PreAuthorize("hasRole('PRODUCTION')")
  public ProductionDailyResponse saveBatch(
      @RequestBody @Valid ProductionDailyBatchRequest request) {
    return productionDailyService.saveBatch(request);
  }

  @GetMapping("/export")
  @PreAuthorize("hasRole('PRODUCTION')")
  public void export(HttpServletResponse response, ProductionDailyQueryDto query)
      throws IOException {
    productionDailyService.export(response, query);
  }
}
