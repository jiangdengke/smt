package org.jdk.project.service;

import static org.jdk.project.utils.StringCaseUtils.convertCamelCaseToSnake;

import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.PageRequestDto;
import org.jdk.project.dto.PageResponseDto;
import org.jdk.project.dto.repair.RepairRecordQueryDto;
import org.jdk.project.dto.repair.RepairRecordRequest;
import org.jdk.project.dto.repair.RepairRecordViewDto;
import org.jdk.project.exception.BusinessException;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.jooq.generated.tables.*;
import org.jooq.generated.tables.records.RepairRecordRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 维修记录服务。 */
@Service
@RequiredArgsConstructor
public class RepairRecordService {

  private static final RepairRecord REPAIR_RECORD = RepairRecord.REPAIR_RECORD;
  private static final RepairRecordPerson REPAIR_RECORD_PERSON =
      RepairRecordPerson.REPAIR_RECORD_PERSON;
  private static final SysFactory SYS_FACTORY = SysFactory.SYS_FACTORY;
  private static final SysWorkshop SYS_WORKSHOP = SysWorkshop.SYS_WORKSHOP;
  private static final SysLine SYS_LINE = SysLine.SYS_LINE;
  private static final SysModel SYS_MODEL = SysModel.SYS_MODEL;
  private static final SysMachine SYS_MACHINE = SysMachine.SYS_MACHINE;
  private static final SysAbnormalCategory SYS_ABNORMAL_CATEGORY =
      SysAbnormalCategory.SYS_ABNORMAL_CATEGORY;
  private static final SysAbnormalType SYS_ABNORMAL_TYPE = SysAbnormalType.SYS_ABNORMAL_TYPE;
  private static final SysTeam SYS_TEAM = SysTeam.SYS_TEAM;
  private static final SysPerson SYS_PERSON = SysPerson.SYS_PERSON;

  private final DSLContext dsl;

  public PageResponseDto<List<RepairRecordViewDto>> list(
      RepairRecordQueryDto query, PageRequestDto pageRequest) {
    Condition condition = buildCondition(query);
    long total = dsl.selectCount().from(REPAIR_RECORD).where(condition).fetchOne(0, long.class);
    if (total == 0) {
      return new PageResponseDto<>(0, List.of());
    }
    List<RepairRecordViewDto> records = fetchRecords(condition, pageRequest);
    attachRepairPeople(records);
    return new PageResponseDto<>(total, records);
  }

  public RepairRecordViewDto get(Long id) {
    Condition condition = REPAIR_RECORD.ID.eq(id);
    List<RepairRecordViewDto> records = fetchRecords(condition, null);
    if (records.isEmpty()) {
      throw new BusinessException("维修记录不存在");
    }
    attachRepairPeople(records);
    return records.get(0);
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long create(RepairRecordRequest request) {
    String shift = normalizeShift(request.getShift());
    validateRepairState(request.getIsFixed(), request.getFixedAt(), request.getRepairMinutes());
    RepairRecordRecord record = dsl.newRecord(REPAIR_RECORD);
    record.setOccurAt(request.getOccurAt());
    record.setShift(shift);
    record.setFactoryId(request.getFactoryId());
    record.setWorkshopId(request.getWorkshopId());
    record.setLineId(request.getLineId());
    record.setModelId(request.getModelId());
    record.setMachineId(request.getMachineId());
    record.setAbnormalCategoryId(request.getAbnormalCategoryId());
    record.setAbnormalTypeId(request.getAbnormalTypeId());
    record.setAbnormalDesc(normalizeText(request.getAbnormalDesc()));
    record.setSolution(normalizeText(request.getSolution()));
    record.setIsFixed(request.getIsFixed());
    record.setFixedAt(request.getFixedAt());
    record.setRepairMinutes(request.getRepairMinutes());
    record.setTeamId(request.getTeamId());
    record.setResponsiblePersonId(request.getResponsiblePersonId());
    record.store();
    Long recordId = toLongId(record.getId());
    if (recordId == null) {
      throw new BusinessException("维修记录创建失败");
    }
    insertRepairPeople(recordId, request.getRepairPersonIds());
    return recordId;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void update(Long id, RepairRecordRequest request) {
    String shift = normalizeShift(request.getShift());
    validateRepairState(request.getIsFixed(), request.getFixedAt(), request.getRepairMinutes());
    int updated =
        dsl.update(REPAIR_RECORD)
            .set(REPAIR_RECORD.OCCUR_AT, request.getOccurAt())
            .set(REPAIR_RECORD.SHIFT, shift)
            .set(REPAIR_RECORD.FACTORY_ID, request.getFactoryId())
            .set(REPAIR_RECORD.WORKSHOP_ID, request.getWorkshopId())
            .set(REPAIR_RECORD.LINE_ID, request.getLineId())
            .set(REPAIR_RECORD.MODEL_ID, request.getModelId())
            .set(REPAIR_RECORD.MACHINE_ID, request.getMachineId())
            .set(REPAIR_RECORD.ABNORMAL_CATEGORY_ID, request.getAbnormalCategoryId())
            .set(REPAIR_RECORD.ABNORMAL_TYPE_ID, request.getAbnormalTypeId())
            .set(REPAIR_RECORD.ABNORMAL_DESC, normalizeText(request.getAbnormalDesc()))
            .set(REPAIR_RECORD.SOLUTION, normalizeText(request.getSolution()))
            .set(REPAIR_RECORD.IS_FIXED, request.getIsFixed())
            .set(REPAIR_RECORD.FIXED_AT, request.getFixedAt())
            .set(REPAIR_RECORD.REPAIR_MINUTES, request.getRepairMinutes())
            .set(REPAIR_RECORD.TEAM_ID, request.getTeamId())
            .set(REPAIR_RECORD.RESPONSIBLE_PERSON_ID, request.getResponsiblePersonId())
            .where(REPAIR_RECORD.ID.eq(id))
            .execute();
    if (updated <= 0) {
      throw new BusinessException("维修记录不存在");
    }
    dsl.deleteFrom(REPAIR_RECORD_PERSON)
        .where(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID.eq(id))
        .execute();
    insertRepairPeople(id, request.getRepairPersonIds());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void delete(Long id) {
    dsl.deleteFrom(REPAIR_RECORD_PERSON)
        .where(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID.eq(id))
        .execute();
    int deleted = dsl.deleteFrom(REPAIR_RECORD).where(REPAIR_RECORD.ID.eq(id)).execute();
    if (deleted <= 0) {
      throw new BusinessException("维修记录不存在");
    }
  }

  private List<RepairRecordViewDto> fetchRecords(Condition condition, PageRequestDto pageRequest) {
    var select =
        dsl.select(
                REPAIR_RECORD.ID,
                REPAIR_RECORD.OCCUR_AT,
                REPAIR_RECORD.SHIFT,
                REPAIR_RECORD.FACTORY_ID,
                SYS_FACTORY.NAME.as("factory_name"),
                REPAIR_RECORD.WORKSHOP_ID,
                SYS_WORKSHOP.NAME.as("workshop_name"),
                REPAIR_RECORD.LINE_ID,
                SYS_LINE.NAME.as("line_name"),
                REPAIR_RECORD.MODEL_ID,
                SYS_MODEL.NAME.as("model_name"),
                REPAIR_RECORD.MACHINE_ID,
                SYS_MACHINE.MACHINE_NO.as("machine_no"),
                REPAIR_RECORD.ABNORMAL_CATEGORY_ID,
                SYS_ABNORMAL_CATEGORY.NAME.as("abnormal_category_name"),
                REPAIR_RECORD.ABNORMAL_TYPE_ID,
                SYS_ABNORMAL_TYPE.NAME.as("abnormal_type_name"),
                REPAIR_RECORD.ABNORMAL_DESC,
                REPAIR_RECORD.SOLUTION,
                REPAIR_RECORD.IS_FIXED,
                REPAIR_RECORD.FIXED_AT,
                REPAIR_RECORD.REPAIR_MINUTES,
                REPAIR_RECORD.TEAM_ID,
                SYS_TEAM.NAME.as("team_name"),
                REPAIR_RECORD.RESPONSIBLE_PERSON_ID,
                SYS_PERSON.NAME.as("responsible_person_name"))
            .from(REPAIR_RECORD)
            .join(SYS_FACTORY)
            .on(REPAIR_RECORD.FACTORY_ID.eq(SYS_FACTORY.ID))
            .join(SYS_WORKSHOP)
            .on(REPAIR_RECORD.WORKSHOP_ID.eq(SYS_WORKSHOP.ID))
            .join(SYS_LINE)
            .on(REPAIR_RECORD.LINE_ID.eq(SYS_LINE.ID))
            .join(SYS_MODEL)
            .on(REPAIR_RECORD.MODEL_ID.eq(SYS_MODEL.ID))
            .join(SYS_MACHINE)
            .on(REPAIR_RECORD.MACHINE_ID.eq(SYS_MACHINE.ID))
            .join(SYS_ABNORMAL_CATEGORY)
            .on(REPAIR_RECORD.ABNORMAL_CATEGORY_ID.eq(SYS_ABNORMAL_CATEGORY.ID))
            .join(SYS_ABNORMAL_TYPE)
            .on(REPAIR_RECORD.ABNORMAL_TYPE_ID.eq(SYS_ABNORMAL_TYPE.ID))
            .join(SYS_TEAM)
            .on(REPAIR_RECORD.TEAM_ID.eq(SYS_TEAM.ID))
            .join(SYS_PERSON)
            .on(REPAIR_RECORD.RESPONSIBLE_PERSON_ID.eq(SYS_PERSON.ID))
            .where(condition);

    List<SortField<?>> orderBy = buildSortFields(pageRequest);
    var ordered = orderBy.isEmpty() ? select : select.orderBy(orderBy);
    if (pageRequest == null) {
      return ordered.limit(1).fetchInto(RepairRecordViewDto.class);
    }
    int limit = Math.toIntExact(pageRequest.getSize());
    int offset = Math.toIntExact(pageRequest.getOffset());
    return ordered.limit(limit).offset(offset).fetchInto(RepairRecordViewDto.class);
  }

  private void attachRepairPeople(List<RepairRecordViewDto> records) {
    if (records.isEmpty()) {
      return;
    }
    List<Long> recordIds = records.stream().map(RepairRecordViewDto::getId).toList();
    Map<Long, List<Long>> personMap =
        dsl.select(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID, REPAIR_RECORD_PERSON.PERSON_ID)
            .from(REPAIR_RECORD_PERSON)
            .where(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID.in(recordIds))
            .fetchGroups(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID, REPAIR_RECORD_PERSON.PERSON_ID);
    for (RepairRecordViewDto record : records) {
      record.setRepairPersonIds(personMap.getOrDefault(record.getId(), List.of()));
    }
  }

  private void insertRepairPeople(Long recordId, List<Long> repairPersonIds) {
    if (repairPersonIds == null || repairPersonIds.isEmpty()) {
      return;
    }
    List<Long> uniquePersonIds = repairPersonIds.stream().distinct().toList();
    var inserts =
        uniquePersonIds.stream()
            .map(
                personId ->
                    dsl.insertInto(REPAIR_RECORD_PERSON)
                        .columns(
                            REPAIR_RECORD_PERSON.REPAIR_RECORD_ID,
                            REPAIR_RECORD_PERSON.PERSON_ID)
                        .values(recordId, personId))
            .toList();
    dsl.batch(inserts).execute();
  }

  private Condition buildCondition(RepairRecordQueryDto query) {
    if (query == null) {
      return DSL.trueCondition();
    }
    Condition condition = DSL.trueCondition();
    LocalDateTime occurFrom = query.getOccurFrom();
    LocalDateTime occurTo = query.getOccurTo();
    if (occurFrom != null) {
      condition = condition.and(REPAIR_RECORD.OCCUR_AT.ge(occurFrom));
    }
    if (occurTo != null) {
      condition = condition.and(REPAIR_RECORD.OCCUR_AT.le(occurTo));
    }
    if (StringUtils.isNotBlank(query.getShift())) {
      condition = condition.and(REPAIR_RECORD.SHIFT.eq(normalizeShift(query.getShift())));
    }
    if (query.getFactoryId() != null) {
      condition = condition.and(REPAIR_RECORD.FACTORY_ID.eq(query.getFactoryId()));
    }
    if (query.getWorkshopId() != null) {
      condition = condition.and(REPAIR_RECORD.WORKSHOP_ID.eq(query.getWorkshopId()));
    }
    if (query.getLineId() != null) {
      condition = condition.and(REPAIR_RECORD.LINE_ID.eq(query.getLineId()));
    }
    if (query.getModelId() != null) {
      condition = condition.and(REPAIR_RECORD.MODEL_ID.eq(query.getModelId()));
    }
    if (query.getMachineId() != null) {
      condition = condition.and(REPAIR_RECORD.MACHINE_ID.eq(query.getMachineId()));
    }
    if (query.getAbnormalCategoryId() != null) {
      condition = condition.and(REPAIR_RECORD.ABNORMAL_CATEGORY_ID.eq(query.getAbnormalCategoryId()));
    }
    if (query.getAbnormalTypeId() != null) {
      condition = condition.and(REPAIR_RECORD.ABNORMAL_TYPE_ID.eq(query.getAbnormalTypeId()));
    }
    if (query.getIsFixed() != null) {
      condition = condition.and(REPAIR_RECORD.IS_FIXED.eq(query.getIsFixed()));
    }
    if (query.getTeamId() != null) {
      condition = condition.and(REPAIR_RECORD.TEAM_ID.eq(query.getTeamId()));
    }
    if (query.getResponsiblePersonId() != null) {
      condition = condition.and(REPAIR_RECORD.RESPONSIBLE_PERSON_ID.eq(query.getResponsiblePersonId()));
    }
    return condition;
  }

  private List<SortField<?>> buildSortFields(PageRequestDto pageRequest) {
    if (pageRequest == null || pageRequest.getSortBy() == null || pageRequest.getSortBy().isEmpty()) {
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

  private String normalizeText(String value) {
    return StringUtils.trimToNull(value);
  }

  private Long toLongId(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Long longValue) {
      return longValue;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    String text = value.toString();
    if (text.isBlank()) {
      return null;
    }
    return Long.valueOf(text);
  }
}
