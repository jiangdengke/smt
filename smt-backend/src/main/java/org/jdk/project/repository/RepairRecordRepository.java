package org.jdk.project.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.repair.RepairRecordViewDto;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.generated.tables.*;
import org.springframework.stereotype.Repository;

/** 维修记录数据访问。 */
@Repository
@RequiredArgsConstructor
public class RepairRecordRepository {

  private static final RepairRecord REPAIR_RECORD = RepairRecord.REPAIR_RECORD;
  private static final RepairRecordPerson REPAIR_RECORD_PERSON =
      RepairRecordPerson.REPAIR_RECORD_PERSON;

  private final DSLContext dsl;

  public long count(Condition condition) {
    return dsl.selectCount().from(REPAIR_RECORD).where(condition).fetchOne(0, long.class);
  }

  public List<RepairRecordViewDto> fetchRecords(
      Condition condition, List<SortField<?>> orderBy, Integer limit, Integer offset) {
    var select =
        dsl.select(
                REPAIR_RECORD.ID,
                REPAIR_RECORD.OCCUR_AT,
                REPAIR_RECORD.SHIFT,
                REPAIR_RECORD.FACTORY_NAME,
                REPAIR_RECORD.WORKSHOP_NAME,
                REPAIR_RECORD.LINE_NAME,
                REPAIR_RECORD.MACHINE_NO,
                REPAIR_RECORD.ABNORMAL_CATEGORY_NAME,
                REPAIR_RECORD.ABNORMAL_TYPE_NAME,
                REPAIR_RECORD.ABNORMAL_DESC,
                REPAIR_RECORD.SOLUTION,
                REPAIR_RECORD.IS_FIXED,
                REPAIR_RECORD.FIXED_AT,
                REPAIR_RECORD.REPAIR_MINUTES,
                REPAIR_RECORD.TEAM_NAME,
                REPAIR_RECORD.RESPONSIBLE_PERSON_NAME)
            .from(REPAIR_RECORD)
            .where(condition);

    var ordered =
        orderBy == null || orderBy.isEmpty()
            ? select.orderBy(REPAIR_RECORD.ID.desc())
            : select.orderBy(orderBy);
    if (limit == null || offset == null) {
      return ordered.fetchInto(RepairRecordViewDto.class);
    }
    String sql = ordered.getSQL() + " offset ? rows fetch next ? rows only";
    List<Object> params = new ArrayList<>(ordered.getBindValues());
    params.add(offset);
    params.add(limit);
    return dsl.resultQuery(sql, params.toArray()).fetchInto(RepairRecordViewDto.class);
  }

  public Map<Long, List<String>> fetchRepairPersonNameMap(List<Long> recordIds) {
    if (recordIds == null || recordIds.isEmpty()) {
      return Map.of();
    }
    return dsl.select(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID, REPAIR_RECORD_PERSON.PERSON_NAME)
        .from(REPAIR_RECORD_PERSON)
        .where(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID.in(recordIds))
        .fetchGroups(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID, REPAIR_RECORD_PERSON.PERSON_NAME);
  }

  public Long insertRecord(
      LocalDateTime occurAt,
      String shift,
      String factoryName,
      String workshopName,
      String lineName,
      String machineNo,
      String abnormalCategoryName,
      String abnormalTypeName,
      String teamName,
      String responsiblePersonName,
      String abnormalDesc,
      String solution,
      Boolean isFixed,
      LocalDateTime fixedAt,
      Integer repairMinutes,
      Long sourceProcessId) {
    String sql =
        "insert into smtBackend.repair_record ([occur_at], [shift], [factory_name],"
            + " [workshop_name], [line_name], [machine_no], [abnormal_category_name],"
            + " [abnormal_type_name], [team_name], [responsible_person_name], [abnormal_desc],"
            + " [solution], [is_fixed], [fixed_at], [repair_minutes], [source_process_id])"
            + " output inserted.id values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    return dsl.resultQuery(
            sql,
            occurAt,
            shift,
            factoryName,
            workshopName,
            lineName,
            machineNo,
            abnormalCategoryName,
            abnormalTypeName,
            teamName,
            responsiblePersonName,
            abnormalDesc,
            solution,
            isFixed,
            fixedAt,
            repairMinutes,
            sourceProcessId)
        .fetchOne(0, Long.class);
  }

  public Long fetchIdBySourceProcessId(Long sourceProcessId) {
    if (sourceProcessId == null) {
      return null;
    }
    return dsl.select(REPAIR_RECORD.ID)
        .from(REPAIR_RECORD)
        .where(REPAIR_RECORD.SOURCE_PROCESS_ID.eq(sourceProcessId))
        .fetchOne(REPAIR_RECORD.ID);
  }

  public Long fetchSourceProcessId(Long recordId) {
    if (recordId == null) {
      return null;
    }
    return dsl.select(REPAIR_RECORD.SOURCE_PROCESS_ID)
        .from(REPAIR_RECORD)
        .where(REPAIR_RECORD.ID.eq(recordId))
        .fetchOne(REPAIR_RECORD.SOURCE_PROCESS_ID);
  }

  public int updateRecord(
      Long id,
      LocalDateTime occurAt,
      String shift,
      String factoryName,
      String workshopName,
      String lineName,
      String machineNo,
      String abnormalCategoryName,
      String abnormalTypeName,
      String teamName,
      String responsiblePersonName,
      String abnormalDesc,
      String solution,
      Boolean isFixed,
      LocalDateTime fixedAt,
      Integer repairMinutes) {
    return dsl.update(REPAIR_RECORD)
        .set(REPAIR_RECORD.OCCUR_AT, occurAt)
        .set(REPAIR_RECORD.SHIFT, shift)
        .set(REPAIR_RECORD.FACTORY_NAME, factoryName)
        .set(REPAIR_RECORD.WORKSHOP_NAME, workshopName)
        .set(REPAIR_RECORD.LINE_NAME, lineName)
        .set(REPAIR_RECORD.MACHINE_NO, machineNo)
        .set(REPAIR_RECORD.ABNORMAL_CATEGORY_NAME, abnormalCategoryName)
        .set(REPAIR_RECORD.ABNORMAL_TYPE_NAME, abnormalTypeName)
        .set(REPAIR_RECORD.TEAM_NAME, teamName)
        .set(REPAIR_RECORD.RESPONSIBLE_PERSON_NAME, responsiblePersonName)
        .set(REPAIR_RECORD.ABNORMAL_DESC, abnormalDesc)
        .set(REPAIR_RECORD.SOLUTION, solution)
        .set(REPAIR_RECORD.IS_FIXED, isFixed)
        .set(REPAIR_RECORD.FIXED_AT, fixedAt)
        .set(REPAIR_RECORD.REPAIR_MINUTES, repairMinutes)
        .where(REPAIR_RECORD.ID.eq(id))
        .execute();
  }

  public int deleteRecord(Long id) {
    return dsl.deleteFrom(REPAIR_RECORD).where(REPAIR_RECORD.ID.eq(id)).execute();
  }

  public int deleteRepairPeopleByRecordId(Long recordId) {
    return dsl.deleteFrom(REPAIR_RECORD_PERSON)
        .where(REPAIR_RECORD_PERSON.REPAIR_RECORD_ID.eq(recordId))
        .execute();
  }

  public void insertRepairPeople(Long recordId, List<String> personNames) {
    if (personNames == null || personNames.isEmpty()) {
      return;
    }
    List<String> uniqueNames = personNames.stream().filter(Objects::nonNull).distinct().toList();
    var inserts =
        uniqueNames.stream()
            .map(
                personName ->
                    dsl.insertInto(REPAIR_RECORD_PERSON)
                        .columns(
                            REPAIR_RECORD_PERSON.REPAIR_RECORD_ID, REPAIR_RECORD_PERSON.PERSON_NAME)
                        .values(recordId, personName))
            .toList();
    dsl.batch(inserts).execute();
  }
}
