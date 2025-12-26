package org.jdk.project.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.workorder.RepairWorkOrderViewDto;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.generated.tables.RepairWorkOrder;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

/** 维修工单数据访问。 */
@Repository
@RequiredArgsConstructor
public class RepairWorkOrderRepository {

  private static final RepairWorkOrder REPAIR_WORK_ORDER = RepairWorkOrder.REPAIR_WORK_ORDER;

  private final DSLContext dsl;

  public RepairWorkOrderViewDto fetchActiveByProcessId(Long processId) {
    return dsl.selectFrom(REPAIR_WORK_ORDER)
        .where(REPAIR_WORK_ORDER.SOURCE_PROCESS_ID.eq(processId))
        .and(REPAIR_WORK_ORDER.STATUS.in("OPEN", "IN_PROGRESS"))
        .orderBy(REPAIR_WORK_ORDER.ID.desc())
        .fetchOneInto(RepairWorkOrderViewDto.class);
  }

  public RepairWorkOrderViewDto fetchById(Long id) {
    return dsl.selectFrom(REPAIR_WORK_ORDER)
        .where(REPAIR_WORK_ORDER.ID.eq(id))
        .fetchOneInto(RepairWorkOrderViewDto.class);
  }

  public List<RepairWorkOrderViewDto> listByStatus(String status) {
    Condition condition = DSL.noCondition();
    if (status != null) {
      condition = condition.and(REPAIR_WORK_ORDER.STATUS.eq(status));
    }
    return dsl.selectFrom(REPAIR_WORK_ORDER)
        .where(condition)
        .orderBy(REPAIR_WORK_ORDER.CREATED_AT.desc())
        .fetchInto(RepairWorkOrderViewDto.class);
  }

  public Long insertWorkOrder(
      Long sourceProcessId,
      LocalDate prodDate,
      String shift,
      String processName,
      String productCode,
      String seriesName,
      String fa) {
    String sql =
        "insert into smtBackend.repair_work_order "
            + "([source_process_id], [prod_date], [shift], [process_name], [product_code], "
            + "[series_name], [fa]) output inserted.id values (?, ?, ?, ?, ?, ?, ?)";
    return dsl.resultQuery(
            sql, sourceProcessId, prodDate, shift, processName, productCode, seriesName, fa)
        .fetchOne(0, Long.class);
  }

  public int updateWorkOrderStatus(Long id, String status) {
    return dsl.update(REPAIR_WORK_ORDER)
        .set(REPAIR_WORK_ORDER.STATUS, status)
        .set(REPAIR_WORK_ORDER.UPDATED_AT, LocalDateTime.now())
        .where(REPAIR_WORK_ORDER.ID.eq(id))
        .execute();
  }

  public int updateWorkOrderFa(Long id, String fa) {
    return dsl.update(REPAIR_WORK_ORDER)
        .set(REPAIR_WORK_ORDER.FA, fa)
        .set(REPAIR_WORK_ORDER.UPDATED_AT, LocalDateTime.now())
        .where(REPAIR_WORK_ORDER.ID.eq(id))
        .execute();
  }

  public int closeActiveByProcessId(Long processId) {
    return dsl.update(REPAIR_WORK_ORDER)
        .set(REPAIR_WORK_ORDER.STATUS, "DONE")
        .set(REPAIR_WORK_ORDER.UPDATED_AT, LocalDateTime.now())
        .where(REPAIR_WORK_ORDER.SOURCE_PROCESS_ID.eq(processId))
        .and(REPAIR_WORK_ORDER.STATUS.in("OPEN", "IN_PROGRESS"))
        .execute();
  }
}
