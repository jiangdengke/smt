package org.jdk.project.service;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.production.ProductionDailyBatchRequest;
import org.jdk.project.dto.production.ProductionDailyExportDto;
import org.jdk.project.dto.production.ProductionDailyProcessRequest;
import org.jdk.project.dto.production.ProductionDailyProcessViewDto;
import org.jdk.project.dto.production.ProductionDailyQueryDto;
import org.jdk.project.dto.production.ProductionDailyResponse;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.ProductionDailyRepository;
import org.jdk.project.repository.RepairRecordRepository;
import org.jdk.project.utils.excel.ColumnCenterStyleStrategy;
import org.jdk.project.utils.excel.ColumnFillStyleStrategy;
import org.jdk.project.utils.excel.CompactColumnWidthStyleStrategy;
import org.jdk.project.utils.excel.GroupColMergeStrategy;
import org.jdk.project.utils.excel.SummaryRowMergeStrategy;
import org.jdk.project.utils.excel.SummaryRowFillStyleStrategy;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 每日产能服务。 */
@Service
@RequiredArgsConstructor
public class ProductionDailyService {

  private final ProductionDailyRepository productionDailyRepository;
  private final RepairRecordRepository repairRecordRepository;

  public ProductionDailyResponse get(
      LocalDate prodDate, String shift, String factoryName, String workshopName, String lineName) {
    String normalizedShift = normalizeShift(shift);
    String normalizedFactory = normalizeRequiredText(factoryName, "厂区不能为空");
    String normalizedWorkshop = normalizeRequiredText(workshopName, "车间不能为空");
    String normalizedLine = normalizeRequiredText(lineName, "线别不能为空");
    Long headerId =
        productionDailyRepository.findHeaderId(
            prodDate, normalizedShift, normalizedFactory, normalizedWorkshop, normalizedLine);
    ProductionDailyResponse response = new ProductionDailyResponse();
    response.setHeaderId(headerId);
    response.setProdDate(prodDate);
    response.setShift(normalizedShift);
    response.setFactoryName(normalizedFactory);
    response.setWorkshopName(normalizedWorkshop);
    response.setLineName(normalizedLine);
    if (headerId == null) {
      response.setProcesses(List.of());
      return response;
    }
    response.setProcesses(productionDailyRepository.fetchProcessesByHeaderId(headerId));
    return response;
  }

  @Transactional(rollbackFor = Throwable.class)
  public ProductionDailyResponse saveBatch(ProductionDailyBatchRequest request) {
    String shift = normalizeShift(request.getShift());
    LocalDate prodDate = request.getProdDate();
    String factoryName = normalizeRequiredText(request.getFactoryName(), "厂区不能为空");
    String workshopName = normalizeRequiredText(request.getWorkshopName(), "车间不能为空");
    String lineName = normalizeRequiredText(request.getLineName(), "线别不能为空");

    Long headerId =
        productionDailyRepository.findHeaderId(
            prodDate, shift, factoryName, workshopName, lineName);
    if (headerId == null) {
      headerId =
          productionDailyRepository.insertHeader(
              prodDate, shift, factoryName, workshopName, lineName);
      if (headerId == null) {
        headerId =
            productionDailyRepository.findHeaderId(
                prodDate, shift, factoryName, workshopName, lineName);
      }
    }
    if (headerId == null) {
      throw new BusinessException("产能班别创建失败");
    }
    for (ProductionDailyProcessRequest process : request.getProcesses()) {
      saveProcess(headerId, prodDate, shift, factoryName, workshopName, lineName, process);
    }
    ProductionDailyResponse response = new ProductionDailyResponse();
    response.setHeaderId(headerId);
    response.setProdDate(prodDate);
    response.setShift(shift);
    response.setFactoryName(factoryName);
    response.setWorkshopName(workshopName);
    response.setLineName(lineName);
    response.setProcesses(productionDailyRepository.fetchProcessesByHeaderId(headerId));
    return response;
  }

  public void export(HttpServletResponse response, ProductionDailyQueryDto query)
      throws IOException {
    LocalDate from = query.getFrom();
    LocalDate to = query.getTo();
    if (from == null || to == null) {
      throw new BusinessException("导出日期范围不能为空");
    }
    String shift = normalizeShiftNullable(query.getShift());
    String factoryName = normalizeText(query.getFactoryName());
    String workshopName = normalizeText(query.getWorkshopName());
    String lineName = normalizeText(query.getLineName());
    List<ProductionDailyProcessViewDto> records =
        productionDailyRepository.fetchProcessesForExport(
            from, to, shift, factoryName, workshopName, lineName);
    writeExport(response, records);
  }

  public List<ProductionDailyProcessViewDto> listRecords() {
    return productionDailyRepository.fetchAllProcesses();
  }

  public void exportSelected(HttpServletResponse response, List<Long> ids) throws IOException {
    if (ids == null || ids.isEmpty()) {
      throw new BusinessException("请选择要导出的记录");
    }
    List<ProductionDailyProcessViewDto> records =
        productionDailyRepository.fetchProcessesByIds(ids);
    if (records.isEmpty()) {
      throw new BusinessException("未找到导出记录");
    }
    validateSameGroup(records);
    writeExport(response, records);
  }

  private void saveProcess(
      Long headerId,
      LocalDate prodDate,
      String shift,
      String factoryName,
      String workshopName,
      String lineName,
      ProductionDailyProcessRequest process) {
    String machineNo = normalizeRequiredText(process.getMachineNo(), "机台号不能为空");
    String processName = normalizeRequiredText(process.getProcessName(), "制程段不能为空");
    String productCode = normalizeRequiredText(process.getProductCode(), "生产料号不能为空");
    String seriesName = normalizeRequiredText(process.getSeriesName(), "系列不能为空");
    BigDecimal ct = process.getCt();
    Integer equipmentCount = process.getEquipmentCount();
    Integer runMinutes = process.getRunMinutes();
    Integer targetOutput = process.getTargetOutput();
    Integer actualOutput = process.getActualOutput();
    Integer downMinutes = process.getDownMinutes();
    if (ct == null) {
      throw new BusinessException("CT不能为空");
    }
    validateNonNegative(equipmentCount, "投入设备量不能为负数");
    validateNonNegative(runMinutes, "投产时间不能为负数");
    validateNonNegative(targetOutput, "目标产能不能为负数");
    validateNonNegative(actualOutput, "实际产出不能为负数");
    validateNonNegative(downMinutes, "理论Down机时间不能为负数");

    Integer gap = calculateGap(targetOutput, actualOutput);
    BigDecimal achievementRate = calculateAchievementRate(targetOutput, actualOutput);
    String fa = normalizeText(process.getFa());
    String ca = normalizeText(process.getCa());

    Long processId = process.getId();
    if (processId == null) {
      processId =
          productionDailyRepository.insertProcess(
              headerId,
              machineNo,
              processName,
              productCode,
              seriesName,
              ct,
              equipmentCount,
              runMinutes,
              targetOutput,
              actualOutput,
              gap,
              achievementRate,
              downMinutes,
              fa,
              ca);
    } else {
      int updated =
          productionDailyRepository.updateProcess(
              processId,
              headerId,
              machineNo,
              processName,
              productCode,
              seriesName,
              ct,
              equipmentCount,
              runMinutes,
              targetOutput,
              actualOutput,
              gap,
              achievementRate,
              downMinutes,
              fa,
              ca);
      if (updated <= 0) {
        throw new BusinessException("产能明细不存在");
      }
    }
    if (processId == null) {
      throw new BusinessException("产能明细保存失败");
    }
    syncRepairRecord(processId, fa);
  }

  private void syncRepairRecord(Long processId, String fa) {
    if (fa == null) {
      return;
    }
    Long existingId = repairRecordRepository.fetchIdBySourceProcessId(processId);
    if (existingId != null) {
      return;
    }
    ProductionDailyProcessViewDto snapshot =
        productionDailyRepository.fetchProcessSnapshot(processId);
    if (snapshot == null) {
      throw new BusinessException("产能明细不存在");
    }
    repairRecordRepository.insertRecord(
        snapshot.getProdDate().atStartOfDay(),
        snapshot.getShift(),
        snapshot.getFactoryName(),
        snapshot.getWorkshopName(),
        snapshot.getLineName(),
        snapshot.getMachineNo(),
        null,
        null,
        null,
        null,
        snapshot.getFa(),
        null,
        false,
        null,
        null,
        snapshot.getDownMinutes(),
        processId);
  }

  private ProductionDailyExportDto convertToExportDto(ProductionDailyProcessViewDto dto) {
    ProductionDailyExportDto export = new ProductionDailyExportDto();
    export.setProdDate(dto.getProdDate() == null ? null : dto.getProdDate().toString());
    export.setShift("DAY".equals(dto.getShift()) ? "白" : "夜"); // Simplified as per image: "白", "夜"
    export.setFactoryName(dto.getFactoryName());
    export.setWorkshopName(dto.getWorkshopName());
    export.setLineName(dto.getLineName());
    export.setMachineNo(dto.getMachineNo());
    export.setProcessName(dto.getProcessName());
    export.setProductCode(dto.getProductCode());
    export.setSeriesName(dto.getSeriesName());
    export.setCt(dto.getCt());
    export.setEquipmentCount(dto.getEquipmentCount());
    export.setRunMinutes(dto.getRunMinutes());
    export.setTargetOutput(dto.getTargetOutput());
    export.setActualOutput(dto.getActualOutput());
    export.setGap(dto.getGap());
    // Format achievement rate
    if (dto.getAchievementRate() != null) {
      export.setAchievementRate(dto.getAchievementRate() + "%");
    } else {
      export.setAchievementRate("-");
    }
    export.setDownMinutes(dto.getDownMinutes());
    export.setFa(dto.getFa());
    export.setCa(dto.getCa());
    return export;
  }

  private void writeExport(HttpServletResponse response, List<ProductionDailyProcessViewDto> records)
      throws IOException {
    List<ProductionDailyExportDto> exportData = buildExportData(records);
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName =
        URLEncoder.encode("每日产能_" + LocalDateTime.now(), StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    EasyExcel.write(response.getOutputStream(), ProductionDailyExportDto.class)
        .registerWriteHandler(
            new GroupColMergeStrategy(
                1, new int[] {0, 1, 2, 3}, new int[] {0, 1, 2, 3}, 5, "合计"))
        .registerWriteHandler(new SummaryRowMergeStrategy(1, 5, "合计", 0, 11))
        .registerWriteHandler(
            new GroupColMergeStrategy(1, new int[] {4}, new int[] {0, 1, 2, 3, 4}))
        .registerWriteHandler(new ColumnCenterStyleStrategy(0, 1, 2, 3, 4))
        .registerWriteHandler(
            new ColumnFillStyleStrategy(IndexedColors.YELLOW.getIndex(), 13))
        .registerWriteHandler(
            new SummaryRowFillStyleStrategy(1, 5, "合计", 0, 18, IndexedColors.GREEN.getIndex()))
        .registerWriteHandler(new CompactColumnWidthStyleStrategy(0))
        .sheet("每日产能")
        .doWrite(exportData);
  }

  private List<ProductionDailyExportDto> buildExportData(
      List<ProductionDailyProcessViewDto> records) {
    List<ProductionDailyExportDto> result = new ArrayList<>();
    if (records == null || records.isEmpty()) {
      return result;
    }
    String currentGroupKey = null;
    List<SummaryBucket> buckets = new ArrayList<>();
    for (ProductionDailyProcessViewDto record : records) {
      String groupKey = buildGroupKey(record);
      if (currentGroupKey != null && !currentGroupKey.equals(groupKey)) {
        appendGroupSummaries(result, buckets);
        buckets.clear();
      }
      currentGroupKey = groupKey;
      result.add(convertToExportDto(record));
      SummaryBucket bucket = findOrCreateBucket(buckets, record);
      bucket.add(record);
    }
    appendGroupSummaries(result, buckets);
    return result;
  }

  private String buildSummaryKey(ProductionDailyProcessViewDto record) {
    return String.valueOf(record.getProdDate())
        + "|"
        + record.getFactoryName()
        + "|"
        + record.getWorkshopName()
        + "|"
        + record.getLineName()
        + "|"
        + record.getProcessName();
  }

  private String buildGroupKey(ProductionDailyProcessViewDto record) {
    return String.valueOf(record.getProdDate())
        + "|"
        + record.getFactoryName()
        + "|"
        + record.getWorkshopName()
        + "|"
        + record.getLineName();
  }

  private SummaryBucket findOrCreateBucket(
      List<SummaryBucket> summaries, ProductionDailyProcessViewDto record) {
    String key = buildSummaryKey(record);
    for (SummaryBucket bucket : summaries) {
      if (bucket.key.equals(key)) {
        return bucket;
      }
    }
    SummaryBucket bucket = new SummaryBucket(record, key);
    summaries.add(bucket);
    return bucket;
  }

  private void appendGroupSummaries(List<ProductionDailyExportDto> result, List<SummaryBucket> buckets) {
    for (SummaryBucket bucket : buckets) {
      result.add(toSummaryRow(bucket));
    }
  }

  private ProductionDailyExportDto toSummaryRow(SummaryBucket bucket) {
    ProductionDailyExportDto summary = new ProductionDailyExportDto();
    String label = bucket.processName + "合计";
    summary.setProdDate(label);
    summary.setFactoryName(null);
    summary.setWorkshopName(null);
    summary.setLineName(null);
    summary.setShift("");
    summary.setProcessName(label);
    summary.setTargetOutput(bucket.totalTarget);
    summary.setActualOutput(bucket.totalActual);
    if (bucket.totalTarget != null && bucket.totalActual != null) {
      summary.setGap(bucket.totalActual - bucket.totalTarget);
      summary.setAchievementRate(formatRate(bucket.totalTarget, bucket.totalActual));
    } else {
      summary.setGap(null);
      summary.setAchievementRate("-");
    }
    return summary;
  }

  private String formatRate(Integer targetOutput, Integer actualOutput) {
    if (targetOutput == null || targetOutput == 0 || actualOutput == null) {
      return "-";
    }
    BigDecimal rate =
        BigDecimal.valueOf(actualOutput)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(targetOutput), 2, RoundingMode.HALF_UP);
    return rate + "%";
  }

  private static final class SummaryBucket {
    private final String key;
    private final LocalDate prodDate;
    private final String factoryName;
    private final String workshopName;
    private final String lineName;
    private final String processName;
    private Integer totalTarget;
    private Integer totalActual;

    private SummaryBucket(ProductionDailyProcessViewDto record, String key) {
      this.key = key;
      this.prodDate = record.getProdDate();
      this.factoryName = record.getFactoryName();
      this.workshopName = record.getWorkshopName();
      this.lineName = record.getLineName();
      this.processName = record.getProcessName();
    }

    private void add(ProductionDailyProcessViewDto record) {
      if (record.getTargetOutput() != null) {
        totalTarget = totalTarget == null ? record.getTargetOutput() : totalTarget + record.getTargetOutput();
      }
      if (record.getActualOutput() != null) {
        totalActual = totalActual == null ? record.getActualOutput() : totalActual + record.getActualOutput();
      }
    }
  }

  private void validateSameGroup(List<ProductionDailyProcessViewDto> records) {
    ProductionDailyProcessViewDto first = records.get(0);
    for (ProductionDailyProcessViewDto record : records) {
      if (!Objects.equals(first.getProdDate(), record.getProdDate())
          || !Objects.equals(first.getFactoryName(), record.getFactoryName())
          || !Objects.equals(first.getWorkshopName(), record.getWorkshopName())
          || !Objects.equals(first.getLineName(), record.getLineName())) {
        throw new BusinessException("只能导出同一天、同厂区、同车间、同线别的记录");
      }
    }
  }

  private Integer calculateGap(Integer targetOutput, Integer actualOutput) {
    if (targetOutput == null || actualOutput == null) {
      return null;
    }
    return actualOutput - targetOutput;
  }

  private BigDecimal calculateAchievementRate(Integer targetOutput, Integer actualOutput) {
    if (targetOutput == null || targetOutput == 0 || actualOutput == null) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(actualOutput)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(targetOutput), 2, RoundingMode.HALF_UP);
  }

  private String normalizeShift(String shift) {
    String value = StringUtils.upperCase(StringUtils.trimToNull(shift));
    if (value == null) {
      throw new BusinessException("班次不能为空");
    }
    if (!value.equals("DAY") && !value.equals("NIGHT")) {
      throw new BusinessException("班次必须为 DAY 或 NIGHT");
    }
    return value;
  }

  private String normalizeShiftNullable(String shift) {
    if (StringUtils.isBlank(shift)) {
      return null;
    }
    return normalizeShift(shift);
  }

  private String normalizeRequiredText(String value, String message) {
    String normalized = normalizeText(value);
    if (normalized == null) {
      throw new BusinessException(message);
    }
    return normalized;
  }

  private String normalizeText(String value) {
    return StringUtils.trimToNull(value);
  }

  private void validateNonNegative(Integer value, String message) {
    if (value == null) {
      return;
    }
    if (value < 0) {
      throw new BusinessException(message);
    }
  }
}
