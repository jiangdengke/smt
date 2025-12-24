package org.jdk.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.PageRequestDto;
import org.jdk.project.dto.PageResponseDto;
import org.jdk.project.dto.repair.RepairRecordQueryDto;
import org.jdk.project.dto.repair.RepairRecordRequest;
import org.jdk.project.dto.repair.RepairRecordViewDto;
import org.jdk.project.service.RepairRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 维修记录接口。 */
@RestController
@RequestMapping("/repair-records")
@RequiredArgsConstructor
public class RepairRecordController {

  private final RepairRecordService repairRecordService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public Long create(@RequestBody @Valid RepairRecordRequest request) {
    return repairRecordService.create(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public void update(@PathVariable Long id, @RequestBody @Valid RepairRecordRequest request) {
    repairRecordService.update(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    repairRecordService.delete(id);
  }

  @GetMapping("/{id}")
  public RepairRecordViewDto get(@PathVariable Long id) {
    return repairRecordService.get(id);
  }

  @GetMapping
  public PageResponseDto<List<RepairRecordViewDto>> list(
      RepairRecordQueryDto query,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sortBy) {
    PageRequestDto pageRequest = new PageRequestDto(page, size);
    pageRequest.setSortBy(sortBy);
    return repairRecordService.list(query, pageRequest);
  }
}
