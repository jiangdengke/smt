package org.jdk.project.service;

import static org.jdk.project.utils.StringCaseUtils.convertCamelCaseToSnake;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.PageRequestDto;
import org.jdk.project.dto.PageResponseDto;
import org.jdk.project.dto.repair.RepairRecordExportDto;
import org.jdk.project.dto.repair.RepairRecordQueryDto;
import org.jdk.project.dto.repair.RepairRecordRequest;
import org.jdk.project.dto.repair.RepairRecordViewDto;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.RepairRecordRepository;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.generated.tables.RepairRecord;
import org.jooq.generated.tables.RepairRecordPerson;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 维修记录服务。 */
@Service
@RequiredArgsConstructor
public class RepairRecordService {

  private static final RepairRecord REPAIR_RECORD = RepairRecord.REPAIR_RECORD;
  private static final RepairRecordPerson REPAIR_RECORD_PERSON =
      RepairRecordPerson.REPAIR_RECORD_PERSON;

  private final RepairRecordRepository repairRecordRepository;

  public void export(HttpServletResponse response, RepairRecordQueryDto query) throws IOException {
    Condition condition = buildCondition(query);
    // Fetch all records without pagination
    List<RepairRecordViewDto> records =
        repairRecordRepository.fetchRecords(
            condition, List.of(REPAIR_RECORD.OCCUR_AT.desc()), null, null);
    attachRepairPeople(records);

    List<RepairRecordExportDto> exportData =
        records.stream().map(this::convertToExportDto).toList();

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName =
        URLEncoder.encode("维修记录_" + LocalDateTime.now().toString(), StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    EasyExcel.write(response.getOutputStream(), RepairRecordExportDto.class)
        .sheet("维修记录")
        .doWrite(exportData);
  }

  private RepairRecordExportDto convertToExportDto(RepairRecordViewDto dto) {
    RepairRecordExportDto export = new RepairRecordExportDto();
    export.setOccurAt(dto.getOccurAt());
    export.setShift("DAY".equals(dto.getShift()) ? "白班" : "夜班");
    export.setFactoryName(dto.getFactoryName());
    export.setWorkshopName(dto.getWorkshopName());
    export.setLineName(dto.getLineName());
    export.setMachineNo(dto.getMachineNo());
    export.setAbnormalCategoryName(dto.getAbnormalCategoryName());
    export.setAbnormalTypeName(dto.getAbnormalTypeName());
    export.setAbnormalDesc(dto.getAbnormalDesc());
    export.setSolution(dto.getSolution());
    export.setIsFixed(Boolean.TRUE.equals(dto.getIsFixed()) ? "是" : "否");
    export.setFixedAt(dto.getFixedAt());
    export.setRepairMinutes(dto.getRepairMinutes());
    export.setTeamName(dto.getTeamName());
    export.setResponsiblePersonName(dto.getResponsiblePersonName());
    export.setRepairPersonNames(
        dto.getRepairPersonNames() != null ? String.join("、", dto.getRepairPersonNames()) : "");
    return export;
  }

  public PageResponseDto<List<RepairRecordViewDto>> list(
      RepairRecordQueryDto query, PageRequestDto pageRequest) {
    Condition condition = buildCondition(query);
    long total = repairRecordRepository.count(condition);
    if (total == 0) {
      return new PageResponseDto<>(0, List.of());
    }
    List<RepairRecordViewDto> records = fetchRecords(condition, pageRequest);
    return new PageResponseDto<>(total, records);
  }

  public RepairRecordViewDto get(Long id) {
    Condition condition = REPAIR_RECORD.ID.eq(id);
    List<RepairRecordViewDto> records = fetchRecords(condition, null);
    if (records.isEmpty()) {
      throw new BusinessException("维修记录不存在");
    }
    return records.get(0);
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long create(RepairRecordRequest request) {
    String shift = normalizeShift(request.getShift());
    validateRepairState(request.getIsFixed(), request.getFixedAt(), request.getRepairMinutes());
    String factoryName = normalizeRequiredText(request.getFactoryName(), "厂区不能为空");
    String workshopName = normalizeRequiredText(request.getWorkshopName(), "车间不能为空");
    String lineName = normalizeRequiredText(request.getLineName(), "线别不能为空");
    String machineNo = normalizeRequiredText(request.getMachineNo(), "机台号不能为空");
    String abnormalCategoryName =
        normalizeRequiredText(request.getAbnormalCategoryName(), "异常类别不能为空");
    String abnormalTypeName = normalizeRequiredText(request.getAbnormalTypeName(), "异常分类不能为空");
    String teamName = normalizeRequiredText(request.getTeamName(), "组别不能为空");
    String responsiblePersonName =
        normalizeRequiredText(request.getResponsiblePersonName(), "责任人不能为空");
    List<String> repairPersonNames = normalizeRepairPersonNames(request.getRepairPersonNames());
    Long recordId =
        repairRecordRepository.insertRecord(
            request.getOccurAt(),
            shift,
            factoryName,
            workshopName,
            lineName,
            machineNo,
            abnormalCategoryName,
            abnormalTypeName,
            teamName,
            responsiblePersonName,
            normalizeText(request.getAbnormalDesc()),
            normalizeText(request.getSolution()),
            request.getIsFixed(),
            request.getFixedAt(),
            request.getRepairMinutes());
    if (recordId == null) {
      throw new BusinessException("维修记录创建失败");
    }
    repairRecordRepository.insertRepairPeople(recordId, repairPersonNames);
    return recordId;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void update(Long id, RepairRecordRequest request) {
    String shift = normalizeShift(request.getShift());
    validateRepairState(request.getIsFixed(), request.getFixedAt(), request.getRepairMinutes());
    String factoryName = normalizeRequiredText(request.getFactoryName(), "厂区不能为空");
    String workshopName = normalizeRequiredText(request.getWorkshopName(), "车间不能为空");
    String lineName = normalizeRequiredText(request.getLineName(), "线别不能为空");
    String machineNo = normalizeRequiredText(request.getMachineNo(), "机台号不能为空");
    String abnormalCategoryName =
        normalizeRequiredText(request.getAbnormalCategoryName(), "异常类别不能为空");
    String abnormalTypeName = normalizeRequiredText(request.getAbnormalTypeName(), "异常分类不能为空");
    String teamName = normalizeRequiredText(request.getTeamName(), "组别不能为空");
    String responsiblePersonName =
        normalizeRequiredText(request.getResponsiblePersonName(), "责任人不能为空");
    List<String> repairPersonNames = normalizeRepairPersonNames(request.getRepairPersonNames());
    int updated =
        repairRecordRepository.updateRecord(
            id,
            request.getOccurAt(),
            shift,
            factoryName,
            workshopName,
            lineName,
            machineNo,
            abnormalCategoryName,
            abnormalTypeName,
            teamName,
            responsiblePersonName,
            normalizeText(request.getAbnormalDesc()),
            normalizeText(request.getSolution()),
            request.getIsFixed(),
            request.getFixedAt(),
            request.getRepairMinutes());
    if (updated <= 0) {
      throw new BusinessException("维修记录不存在");
    }
    repairRecordRepository.deleteRepairPeopleByRecordId(id);
    repairRecordRepository.insertRepairPeople(id, repairPersonNames);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void delete(Long id) {
    repairRecordRepository.deleteRepairPeopleByRecordId(id);
    int deleted = repairRecordRepository.deleteRecord(id);
    if (deleted <= 0) {
      throw new BusinessException("维修记录不存在");
    }
  }

  private List<RepairRecordViewDto> fetchRecords(Condition condition, PageRequestDto pageRequest) {
    List<SortField<?>> orderBy = buildSortFields(pageRequest);
    Integer limit = null;
    Integer offset = null;
    if (pageRequest != null) {
      limit = Math.toIntExact(pageRequest.getSize());
      offset = Math.toIntExact(pageRequest.getOffset());
    }
    List<RepairRecordViewDto> records =
        repairRecordRepository.fetchRecords(condition, orderBy, limit, offset);
    attachRepairPeople(records);
    return records;
  }

  private void attachRepairPeople(List<RepairRecordViewDto> records) {
    if (records.isEmpty()) {
      return;
    }
    List<Long> recordIds = records.stream().map(RepairRecordViewDto::getId).toList();
    Map<Long, List<String>> personNameMap =
        repairRecordRepository.fetchRepairPersonNameMap(recordIds);
    for (RepairRecordViewDto record : records) {
      record.setRepairPersonNames(personNameMap.getOrDefault(record.getId(), List.of()));
    }
  }

  private Condition buildCondition(RepairRecordQueryDto query) {
    if (query == null) {
      return DSL.condition("1=1");
    }
    Condition condition = DSL.condition("1=1");
    LocalDateTime occurFrom = query.getOccurFrom();
    LocalDateTime occurTo = query.getOccurTo();
    LocalDateTime fixedFrom = query.getFixedFrom();
    LocalDateTime fixedTo = query.getFixedTo();
    if (occurFrom != null) {
      condition = condition.and(REPAIR_RECORD.OCCUR_AT.ge(occurFrom));
    }
    if (occurTo != null) {
      condition = condition.and(REPAIR_RECORD.OCCUR_AT.le(occurTo));
    }
    if (fixedFrom != null) {
      condition = condition.and(REPAIR_RECORD.FIXED_AT.ge(fixedFrom));
    }
    if (fixedTo != null) {
      condition = condition.and(REPAIR_RECORD.FIXED_AT.le(fixedTo));
    }
    if (StringUtils.isNotBlank(query.getShift())) {
      condition = condition.and(REPAIR_RECORD.SHIFT.eq(normalizeShift(query.getShift())));
    }
    String factoryName = normalizeText(query.getFactoryName());
    if (factoryName != null) {
      condition = condition.and(REPAIR_RECORD.FACTORY_NAME.eq(factoryName));
    }
    String workshopName = normalizeText(query.getWorkshopName());
    if (workshopName != null) {
      condition = condition.and(REPAIR_RECORD.WORKSHOP_NAME.eq(workshopName));
    }
    String lineName = normalizeText(query.getLineName());
    if (lineName != null) {
      condition = condition.and(REPAIR_RECORD.LINE_NAME.eq(lineName));
    }
    String machineNo = normalizeText(query.getMachineNo());
    if (machineNo != null) {
      condition = condition.and(REPAIR_RECORD.MACHINE_NO.eq(machineNo));
    }
    String abnormalCategoryName = normalizeText(query.getAbnormalCategoryName());
    if (abnormalCategoryName != null) {
      condition = condition.and(REPAIR_RECORD.ABNORMAL_CATEGORY_NAME.eq(abnormalCategoryName));
    }
    String abnormalTypeName = normalizeText(query.getAbnormalTypeName());
    if (abnormalTypeName != null) {
      condition = condition.and(REPAIR_RECORD.ABNORMAL_TYPE_NAME.eq(abnormalTypeName));
    }
    if (query.getIsFixed() != null) {
      condition = condition.and(REPAIR_RECORD.IS_FIXED.eq(query.getIsFixed()));
    }
    String teamName = normalizeText(query.getTeamName());
    if (teamName != null) {
      condition = condition.and(REPAIR_RECORD.TEAM_NAME.eq(teamName));
    }
    String responsiblePersonName = normalizeText(query.getResponsiblePersonName());
    if (responsiblePersonName != null) {
      condition = condition.and(REPAIR_RECORD.RESPONSIBLE_PERSON_NAME.eq(responsiblePersonName));
    }
    String repairPersonName = normalizeText(query.getRepairPersonName());
    if (repairPersonName != null) {
      condition =
          condition.and(
              DSL.exists(
                  DSL.selectOne()
                      .from(REPAIR_RECORD_PERSON)
                      .where(
                          REPAIR_RECORD_PERSON
                              .REPAIR_RECORD_ID
                              .cast(Long.class)
                              .eq(REPAIR_RECORD.ID.cast(Long.class)))
                      .and(REPAIR_RECORD_PERSON.PERSON_NAME.eq(repairPersonName))));
    }
    return condition;
  }

  private List<SortField<?>> buildSortFields(PageRequestDto pageRequest) {
    if (pageRequest == null
        || pageRequest.getSortBy() == null
        || pageRequest.getSortBy().isEmpty()) {
      return List.of(REPAIR_RECORD.ID.desc());
    }
    List<SortField<?>> sortFields = new ArrayList<>();
    for (var entry : pageRequest.getSortBy().entrySet()) {
      String fieldName = convertCamelCaseToSnake(entry.getKey());
      Field<Object> field = REPAIR_RECORD.field(fieldName, Object.class);
      if (field == null) {
        continue;
      }
      SortOrder order = SortOrder.valueOf(entry.getValue().getKeyword());
      sortFields.add(field.sort(order));
    }
    if (sortFields.isEmpty()) {
      return List.of(REPAIR_RECORD.ID.desc());
    }
    return sortFields;
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

  private void validateRepairState(Boolean isFixed, LocalDateTime fixedAt, Integer repairMinutes) {
    if (isFixed == null) {
      throw new BusinessException("是否已修复不能为空");
    }
    if (Boolean.TRUE.equals(isFixed)) {
      if (fixedAt == null) {
        throw new BusinessException("已修复时必须填写修复时间");
      }
      if (repairMinutes == null) {
        throw new BusinessException("已修复时必须填写维修耗时");
      }
      if (repairMinutes < 0) {
        throw new BusinessException("维修耗时不能为负数");
      }
    } else {
      if (fixedAt != null) {
        throw new BusinessException("未修复时不能填写修复时间");
      }
      if (repairMinutes != null) {
        throw new BusinessException("未修复时不能填写维修耗时");
      }
    }
  }

  private String normalizeRequiredText(String value, String message) {
    String normalized = normalizeText(value);
    if (normalized == null) {
      throw new BusinessException(message);
    }
    return normalized;
  }

  private List<String> normalizeRepairPersonNames(List<String> personNames) {
    if (personNames == null || personNames.isEmpty()) {
      return List.of();
    }
    List<String> normalized = new ArrayList<>();
    for (String name : personNames) {
      String value = normalizeText(name);
      if (value == null) {
        throw new BusinessException("维修人不能为空");
      }
      normalized.add(value);
    }
    return normalized.stream().distinct().toList();
  }

  private String normalizeText(String value) {
    return StringUtils.trimToNull(value);
  }
}
