package org.jdk.project.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.workorder.RepairWorkOrderCompleteRequest;
import org.jdk.project.dto.workorder.RepairWorkOrderViewDto;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.ProductionDailyRepository;
import org.jdk.project.repository.RepairWorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 维修工单服务。 */
@Service
@RequiredArgsConstructor
public class RepairWorkOrderService {

  private final RepairWorkOrderRepository repairWorkOrderRepository;
  private final ProductionDailyRepository productionDailyRepository;

  public List<RepairWorkOrderViewDto> list(String status) {
    String normalizedStatus = normalizeStatusNullable(status);
    return repairWorkOrderRepository.listByStatus(normalizedStatus);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void complete(Long id, RepairWorkOrderCompleteRequest request) {
    RepairWorkOrderViewDto workOrder = repairWorkOrderRepository.fetchById(id);
    if (workOrder == null) {
      throw new BusinessException("工单不存在");
    }
    String ca = normalizeRequiredText(request.getCa(), "解决对策不能为空");
    int updated = productionDailyRepository.updateProcessCa(workOrder.getSourceProcessId(), ca);
    if (updated <= 0) {
      throw new BusinessException("回填失败");
    }
    repairWorkOrderRepository.updateWorkOrderStatus(id, "DONE");
  }

  private String normalizeStatusNullable(String status) {
    if (StringUtils.isBlank(status)) {
      return null;
    }
    String value = StringUtils.upperCase(StringUtils.trimToNull(status));
    if (value == null) {
      return null;
    }
    if (!value.equals("OPEN") && !value.equals("IN_PROGRESS") && !value.equals("DONE")) {
      throw new BusinessException("工单状态不合法");
    }
    return value;
  }

  private String normalizeRequiredText(String value, String message) {
    String normalized = StringUtils.trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(message);
    }
    return normalized;
  }
}
