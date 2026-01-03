package org.jdk.project.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.production.ProductionDailyProcessViewDto;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.generated.tables.ProductionDailyHeader;
import org.jooq.generated.tables.ProductionDailyProcess;
import org.springframework.stereotype.Repository;

/** 每日产能数据访问。 */
@Repository
@RequiredArgsConstructor
public class ProductionDailyRepository {

  private static final ProductionDailyHeader PRODUCTION_DAILY_HEADER =
      ProductionDailyHeader.PRODUCTION_DAILY_HEADER;
  private static final ProductionDailyProcess PRODUCTION_DAILY_PROCESS =
      ProductionDailyProcess.PRODUCTION_DAILY_PROCESS;

  private final DSLContext dsl;

  public Long findHeaderId(LocalDate prodDate, String shift) {
    return dsl.select(PRODUCTION_DAILY_HEADER.ID)
        .from(PRODUCTION_DAILY_HEADER)
        .where(PRODUCTION_DAILY_HEADER.PROD_DATE.eq(prodDate))
        .and(PRODUCTION_DAILY_HEADER.SHIFT.eq(shift))
        .fetchOne(PRODUCTION_DAILY_HEADER.ID);
  }

  public Long insertHeader(LocalDate prodDate, String shift) {
    String sql =
        "insert into smtBackend.production_daily_header ([prod_date], [shift]) "
            + "output inserted.id values (?, ?)";
    return dsl.resultQuery(sql, prodDate, shift).fetchOne(0, Long.class);
  }

  public List<ProductionDailyProcessViewDto> fetchProcessesByHeaderId(Long headerId) {
    return baseSelect()
        .where(PRODUCTION_DAILY_PROCESS.HEADER_ID.eq(headerId))
        .orderBy(PRODUCTION_DAILY_PROCESS.ID.asc())
        .fetchInto(ProductionDailyProcessViewDto.class);
  }

  public List<ProductionDailyProcessViewDto> fetchProcessesByDate(
      LocalDate prodDate, String shift) {
    Condition condition = PRODUCTION_DAILY_HEADER.PROD_DATE.eq(prodDate);
    if (shift != null) {
      condition = condition.and(PRODUCTION_DAILY_HEADER.SHIFT.eq(shift));
    }
    return baseSelect()
        .where(condition)
        .orderBy(PRODUCTION_DAILY_HEADER.SHIFT.asc(), PRODUCTION_DAILY_PROCESS.ID.asc())
        .fetchInto(ProductionDailyProcessViewDto.class);
  }

  public List<ProductionDailyProcessViewDto> fetchProcessesForExport(
      LocalDate from, LocalDate to, String shift) {
    Condition condition =
        PRODUCTION_DAILY_HEADER.PROD_DATE.ge(from).and(PRODUCTION_DAILY_HEADER.PROD_DATE.le(to));
    if (shift != null) {
      condition = condition.and(PRODUCTION_DAILY_HEADER.SHIFT.eq(shift));
    }
    return baseSelect()
        .where(condition)
        .orderBy(PRODUCTION_DAILY_HEADER.PROD_DATE.asc(), PRODUCTION_DAILY_HEADER.SHIFT.asc())
        .fetchInto(ProductionDailyProcessViewDto.class);
  }

  public ProductionDailyProcessViewDto fetchProcessById(Long id) {
    return baseSelect()
        .where(PRODUCTION_DAILY_PROCESS.ID.eq(id))
        .fetchOneInto(ProductionDailyProcessViewDto.class);
  }

  public Long insertProcess(
      Long headerId,
      String processName,
      String productCode,
      String seriesName,
      BigDecimal ct,
      Integer equipmentCount,
      Integer runMinutes,
      Integer targetOutput,
      Integer actualOutput,
      Integer gap,
      BigDecimal achievementRate,
      Integer downMinutes,
      String fa,
      String ca) {
    String sql =
        "insert into smtBackend.production_daily_process "
            + "([header_id], [process_name], [product_code], [series_name], [ct], "
            + "[equipment_count], [run_minutes], [target_output], [actual_output], "
            + "[gap], [achievement_rate], [down_minutes], [fa], [ca]) "
            + "output inserted.id values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    return dsl.resultQuery(
            sql,
            headerId,
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
            ca)
        .fetchOne(0, Long.class);
  }

  public int updateProcess(
      Long id,
      Long headerId,
      String processName,
      String productCode,
      String seriesName,
      BigDecimal ct,
      Integer equipmentCount,
      Integer runMinutes,
      Integer targetOutput,
      Integer actualOutput,
      Integer gap,
      BigDecimal achievementRate,
      Integer downMinutes,
      String fa,
      String ca) {
    return dsl.update(PRODUCTION_DAILY_PROCESS)
        .set(PRODUCTION_DAILY_PROCESS.PROCESS_NAME, processName)
        .set(PRODUCTION_DAILY_PROCESS.PRODUCT_CODE, productCode)
        .set(PRODUCTION_DAILY_PROCESS.SERIES_NAME, seriesName)
        .set(PRODUCTION_DAILY_PROCESS.CT, ct)
        .set(PRODUCTION_DAILY_PROCESS.EQUIPMENT_COUNT, equipmentCount)
        .set(PRODUCTION_DAILY_PROCESS.RUN_MINUTES, runMinutes)
        .set(PRODUCTION_DAILY_PROCESS.TARGET_OUTPUT, targetOutput)
        .set(PRODUCTION_DAILY_PROCESS.ACTUAL_OUTPUT, actualOutput)
        .set(PRODUCTION_DAILY_PROCESS.GAP, gap)
        .set(PRODUCTION_DAILY_PROCESS.ACHIEVEMENT_RATE, achievementRate)
        .set(PRODUCTION_DAILY_PROCESS.DOWN_MINUTES, downMinutes)
        .set(PRODUCTION_DAILY_PROCESS.FA, fa)
        .set(PRODUCTION_DAILY_PROCESS.CA, ca)
        .where(PRODUCTION_DAILY_PROCESS.ID.eq(id))
        .and(PRODUCTION_DAILY_PROCESS.HEADER_ID.eq(headerId))
        .execute();
  }

  public int updateProcessCa(Long id, String ca) {
    return dsl.update(PRODUCTION_DAILY_PROCESS)
        .set(PRODUCTION_DAILY_PROCESS.CA, ca)
        .where(PRODUCTION_DAILY_PROCESS.ID.eq(id))
        .execute();
  }

  private SelectJoinStep<? extends Record> baseSelect() {
    return dsl.select(
            PRODUCTION_DAILY_PROCESS.ID.as("id"),
            PRODUCTION_DAILY_PROCESS.HEADER_ID.as("headerId"),
            PRODUCTION_DAILY_HEADER.PROD_DATE.as("prodDate"),
            PRODUCTION_DAILY_HEADER.SHIFT.as("shift"),
            PRODUCTION_DAILY_PROCESS.PROCESS_NAME.as("processName"),
            PRODUCTION_DAILY_PROCESS.PRODUCT_CODE.as("productCode"),
            PRODUCTION_DAILY_PROCESS.SERIES_NAME.as("seriesName"),
            PRODUCTION_DAILY_PROCESS.CT.as("ct"),
            PRODUCTION_DAILY_PROCESS.EQUIPMENT_COUNT.as("equipmentCount"),
            PRODUCTION_DAILY_PROCESS.RUN_MINUTES.as("runMinutes"),
            PRODUCTION_DAILY_PROCESS.TARGET_OUTPUT.as("targetOutput"),
            PRODUCTION_DAILY_PROCESS.ACTUAL_OUTPUT.as("actualOutput"),
            PRODUCTION_DAILY_PROCESS.GAP.as("gap"),
            PRODUCTION_DAILY_PROCESS.ACHIEVEMENT_RATE.as("achievementRate"),
            PRODUCTION_DAILY_PROCESS.DOWN_MINUTES.as("downMinutes"),
            PRODUCTION_DAILY_PROCESS.FA.as("fa"),
            PRODUCTION_DAILY_PROCESS.CA.as("ca"))
        .from(PRODUCTION_DAILY_PROCESS)
        .join(PRODUCTION_DAILY_HEADER)
        .on(PRODUCTION_DAILY_PROCESS.HEADER_ID.eq(PRODUCTION_DAILY_HEADER.ID));
  }
}
