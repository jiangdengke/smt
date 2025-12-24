package org.jdk.project.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.sys.*;
import org.jdk.project.exception.BusinessException;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.generated.tables.*;
import org.jooq.generated.tables.records.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 系统维护表服务。 */
@Service
@RequiredArgsConstructor
public class SysMasterService {

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

  public List<org.jooq.generated.tables.pojos.SysFactory> listFactories(boolean activeOnly) {
    return dsl.selectFrom(SYS_FACTORY)
        .where(activeCondition(SYS_FACTORY.IS_ACTIVE, activeOnly))
        .orderBy(SYS_FACTORY.SORT_ORDER.asc(), SYS_FACTORY.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysFactory.class);
  }

  public List<org.jooq.generated.tables.pojos.SysWorkshop> listWorkshops(
      Long factoryId, boolean activeOnly) {
    Condition condition = activeCondition(SYS_WORKSHOP.IS_ACTIVE, activeOnly);
    if (factoryId != null) {
      condition = condition.and(SYS_WORKSHOP.FACTORY_ID.eq(factoryId));
    }
    return dsl.selectFrom(SYS_WORKSHOP)
        .where(condition)
        .orderBy(SYS_WORKSHOP.SORT_ORDER.asc(), SYS_WORKSHOP.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysWorkshop.class);
  }

  public List<org.jooq.generated.tables.pojos.SysLine> listLines(
      Long workshopId, boolean activeOnly) {
    Condition condition = activeCondition(SYS_LINE.IS_ACTIVE, activeOnly);
    if (workshopId != null) {
      condition = condition.and(SYS_LINE.WORKSHOP_ID.eq(workshopId));
    }
    return dsl.selectFrom(SYS_LINE)
        .where(condition)
        .orderBy(SYS_LINE.SORT_ORDER.asc(), SYS_LINE.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysLine.class);
  }

  public List<org.jooq.generated.tables.pojos.SysModel> listModels(boolean activeOnly) {
    return dsl.selectFrom(SYS_MODEL)
        .where(activeCondition(SYS_MODEL.IS_ACTIVE, activeOnly))
        .orderBy(SYS_MODEL.SORT_ORDER.asc(), SYS_MODEL.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysModel.class);
  }

  public List<org.jooq.generated.tables.pojos.SysMachine> listMachines(
      Long modelId, boolean activeOnly) {
    Condition condition = activeCondition(SYS_MACHINE.IS_ACTIVE, activeOnly);
    if (modelId != null) {
      condition = condition.and(SYS_MACHINE.MODEL_ID.eq(modelId));
    }
    return dsl.selectFrom(SYS_MACHINE)
        .where(condition)
        .orderBy(SYS_MACHINE.SORT_ORDER.asc(), SYS_MACHINE.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysMachine.class);
  }

  public List<org.jooq.generated.tables.pojos.SysAbnormalCategory> listAbnormalCategories(
      boolean activeOnly) {
    return dsl.selectFrom(SYS_ABNORMAL_CATEGORY)
        .where(activeCondition(SYS_ABNORMAL_CATEGORY.IS_ACTIVE, activeOnly))
        .orderBy(SYS_ABNORMAL_CATEGORY.SORT_ORDER.asc(), SYS_ABNORMAL_CATEGORY.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysAbnormalCategory.class);
  }

  public List<org.jooq.generated.tables.pojos.SysAbnormalType> listAbnormalTypes(
      Long abnormalCategoryId, boolean activeOnly) {
    Condition condition = activeCondition(SYS_ABNORMAL_TYPE.IS_ACTIVE, activeOnly);
    if (abnormalCategoryId != null) {
      condition = condition.and(SYS_ABNORMAL_TYPE.ABNORMAL_CATEGORY_ID.eq(abnormalCategoryId));
    }
    return dsl.selectFrom(SYS_ABNORMAL_TYPE)
        .where(condition)
        .orderBy(SYS_ABNORMAL_TYPE.SORT_ORDER.asc(), SYS_ABNORMAL_TYPE.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysAbnormalType.class);
  }

  public List<org.jooq.generated.tables.pojos.SysTeam> listTeams(boolean activeOnly) {
    return dsl.selectFrom(SYS_TEAM)
        .where(activeCondition(SYS_TEAM.IS_ACTIVE, activeOnly))
        .orderBy(SYS_TEAM.SORT_ORDER.asc(), SYS_TEAM.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysTeam.class);
  }

  public List<org.jooq.generated.tables.pojos.SysPerson> listPeople(
      Long teamId, boolean activeOnly) {
    Condition condition = activeOnly ? SYS_PERSON.IS_ACTIVE.eq(true) : DSL.trueCondition();
    if (teamId != null) {
      condition = condition.and(SYS_PERSON.TEAM_ID.eq(teamId));
    }
    return dsl.selectFrom(SYS_PERSON)
        .where(condition)
        .orderBy(SYS_PERSON.ID.asc())
        .fetchInto(org.jooq.generated.tables.pojos.SysPerson.class);
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createFactory(SysSimpleRequest request) {
    SysFactoryRecord record = dsl.newRecord(SYS_FACTORY);
    fillSimpleFields(record, request);
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateFactory(Long id, SysSimpleRequest request) {
    int updated =
        dsl.update(SYS_FACTORY)
            .set(SYS_FACTORY.NAME, normalizeText(request.getName()))
            .set(SYS_FACTORY.CODE, normalizeText(request.getCode()))
            .set(SYS_FACTORY.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_FACTORY.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_FACTORY.REMARK, normalizeText(request.getRemark()))
            .where(SYS_FACTORY.ID.eq(id))
            .execute();
    assertUpdated(updated, "厂区");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteFactory(Long id) {
    int deleted = dsl.deleteFrom(SYS_FACTORY).where(SYS_FACTORY.ID.eq(id)).execute();
    assertUpdated(deleted, "厂区");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createWorkshop(SysWorkshopRequest request) {
    SysWorkshopRecord record = dsl.newRecord(SYS_WORKSHOP);
    fillSimpleFields(record, request);
    record.setFactoryId(request.getFactoryId());
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateWorkshop(Long id, SysWorkshopRequest request) {
    int updated =
        dsl.update(SYS_WORKSHOP)
            .set(SYS_WORKSHOP.NAME, normalizeText(request.getName()))
            .set(SYS_WORKSHOP.CODE, normalizeText(request.getCode()))
            .set(SYS_WORKSHOP.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_WORKSHOP.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_WORKSHOP.REMARK, normalizeText(request.getRemark()))
            .set(SYS_WORKSHOP.FACTORY_ID, request.getFactoryId())
            .where(SYS_WORKSHOP.ID.eq(id))
            .execute();
    assertUpdated(updated, "车间");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteWorkshop(Long id) {
    int deleted = dsl.deleteFrom(SYS_WORKSHOP).where(SYS_WORKSHOP.ID.eq(id)).execute();
    assertUpdated(deleted, "车间");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createLine(SysLineRequest request) {
    SysLineRecord record = dsl.newRecord(SYS_LINE);
    fillSimpleFields(record, request);
    record.setWorkshopId(request.getWorkshopId());
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateLine(Long id, SysLineRequest request) {
    int updated =
        dsl.update(SYS_LINE)
            .set(SYS_LINE.NAME, normalizeText(request.getName()))
            .set(SYS_LINE.CODE, normalizeText(request.getCode()))
            .set(SYS_LINE.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_LINE.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_LINE.REMARK, normalizeText(request.getRemark()))
            .set(SYS_LINE.WORKSHOP_ID, request.getWorkshopId())
            .where(SYS_LINE.ID.eq(id))
            .execute();
    assertUpdated(updated, "线别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteLine(Long id) {
    int deleted = dsl.deleteFrom(SYS_LINE).where(SYS_LINE.ID.eq(id)).execute();
    assertUpdated(deleted, "线别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createModel(SysSimpleRequest request) {
    SysModelRecord record = dsl.newRecord(SYS_MODEL);
    fillSimpleFields(record, request);
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateModel(Long id, SysSimpleRequest request) {
    int updated =
        dsl.update(SYS_MODEL)
            .set(SYS_MODEL.NAME, normalizeText(request.getName()))
            .set(SYS_MODEL.CODE, normalizeText(request.getCode()))
            .set(SYS_MODEL.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_MODEL.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_MODEL.REMARK, normalizeText(request.getRemark()))
            .where(SYS_MODEL.ID.eq(id))
            .execute();
    assertUpdated(updated, "机型");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteModel(Long id) {
    int deleted = dsl.deleteFrom(SYS_MODEL).where(SYS_MODEL.ID.eq(id)).execute();
    assertUpdated(deleted, "机型");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createMachine(SysMachineRequest request) {
    SysMachineRecord record = dsl.newRecord(SYS_MACHINE);
    record.setModelId(request.getModelId());
    record.setMachineNo(normalizeText(request.getMachineNo()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateMachine(Long id, SysMachineRequest request) {
    int updated =
        dsl.update(SYS_MACHINE)
            .set(SYS_MACHINE.MODEL_ID, request.getModelId())
            .set(SYS_MACHINE.MACHINE_NO, normalizeText(request.getMachineNo()))
            .set(SYS_MACHINE.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_MACHINE.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_MACHINE.REMARK, normalizeText(request.getRemark()))
            .where(SYS_MACHINE.ID.eq(id))
            .execute();
    assertUpdated(updated, "机台");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteMachine(Long id) {
    int deleted = dsl.deleteFrom(SYS_MACHINE).where(SYS_MACHINE.ID.eq(id)).execute();
    assertUpdated(deleted, "机台");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createAbnormalCategory(SysSimpleRequest request) {
    SysAbnormalCategoryRecord record = dsl.newRecord(SYS_ABNORMAL_CATEGORY);
    fillSimpleFields(record, request);
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateAbnormalCategory(Long id, SysSimpleRequest request) {
    int updated =
        dsl.update(SYS_ABNORMAL_CATEGORY)
            .set(SYS_ABNORMAL_CATEGORY.NAME, normalizeText(request.getName()))
            .set(SYS_ABNORMAL_CATEGORY.CODE, normalizeText(request.getCode()))
            .set(SYS_ABNORMAL_CATEGORY.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_ABNORMAL_CATEGORY.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_ABNORMAL_CATEGORY.REMARK, normalizeText(request.getRemark()))
            .where(SYS_ABNORMAL_CATEGORY.ID.eq(id))
            .execute();
    assertUpdated(updated, "异常类别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteAbnormalCategory(Long id) {
    int deleted =
        dsl.deleteFrom(SYS_ABNORMAL_CATEGORY).where(SYS_ABNORMAL_CATEGORY.ID.eq(id)).execute();
    assertUpdated(deleted, "异常类别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createAbnormalType(SysAbnormalTypeRequest request) {
    SysAbnormalTypeRecord record = dsl.newRecord(SYS_ABNORMAL_TYPE);
    fillSimpleFields(record, request);
    record.setAbnormalCategoryId(request.getAbnormalCategoryId());
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateAbnormalType(Long id, SysAbnormalTypeRequest request) {
    int updated =
        dsl.update(SYS_ABNORMAL_TYPE)
            .set(SYS_ABNORMAL_TYPE.NAME, normalizeText(request.getName()))
            .set(SYS_ABNORMAL_TYPE.CODE, normalizeText(request.getCode()))
            .set(SYS_ABNORMAL_TYPE.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_ABNORMAL_TYPE.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_ABNORMAL_TYPE.REMARK, normalizeText(request.getRemark()))
            .set(SYS_ABNORMAL_TYPE.ABNORMAL_CATEGORY_ID, request.getAbnormalCategoryId())
            .where(SYS_ABNORMAL_TYPE.ID.eq(id))
            .execute();
    assertUpdated(updated, "异常分类");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteAbnormalType(Long id) {
    int deleted = dsl.deleteFrom(SYS_ABNORMAL_TYPE).where(SYS_ABNORMAL_TYPE.ID.eq(id)).execute();
    assertUpdated(deleted, "异常分类");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createTeam(SysSimpleRequest request) {
    SysTeamRecord record = dsl.newRecord(SYS_TEAM);
    fillSimpleFields(record, request);
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateTeam(Long id, SysSimpleRequest request) {
    int updated =
        dsl.update(SYS_TEAM)
            .set(SYS_TEAM.NAME, normalizeText(request.getName()))
            .set(SYS_TEAM.CODE, normalizeText(request.getCode()))
            .set(SYS_TEAM.SORT_ORDER, defaultSortOrder(request.getSortOrder()))
            .set(SYS_TEAM.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_TEAM.REMARK, normalizeText(request.getRemark()))
            .where(SYS_TEAM.ID.eq(id))
            .execute();
    assertUpdated(updated, "组别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteTeam(Long id) {
    int deleted = dsl.deleteFrom(SYS_TEAM).where(SYS_TEAM.ID.eq(id)).execute();
    assertUpdated(deleted, "组别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createPerson(SysPersonRequest request) {
    SysPersonRecord record = dsl.newRecord(SYS_PERSON);
    record.setTeamId(request.getTeamId());
    record.setName(normalizeText(request.getName()));
    record.setEmployeeNo(normalizeText(request.getEmployeeNo()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
    record.store();
    return toLongId(record.getId());
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updatePerson(Long id, SysPersonRequest request) {
    int updated =
        dsl.update(SYS_PERSON)
            .set(SYS_PERSON.TEAM_ID, request.getTeamId())
            .set(SYS_PERSON.NAME, normalizeText(request.getName()))
            .set(SYS_PERSON.EMPLOYEE_NO, normalizeText(request.getEmployeeNo()))
            .set(SYS_PERSON.IS_ACTIVE, defaultActive(request.getIsActive()))
            .set(SYS_PERSON.REMARK, normalizeText(request.getRemark()))
            .where(SYS_PERSON.ID.eq(id))
            .execute();
    assertUpdated(updated, "人员");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deletePerson(Long id) {
    int deleted = dsl.deleteFrom(SYS_PERSON).where(SYS_PERSON.ID.eq(id)).execute();
    assertUpdated(deleted, "人员");
  }

  private Condition activeCondition(org.jooq.Field<Boolean> field, boolean activeOnly) {
    return activeOnly ? field.eq(true) : DSL.trueCondition();
  }

  private String normalizeText(String value) {
    return StringUtils.trimToNull(value);
  }

  private int defaultSortOrder(Integer value) {
    return value == null ? 0 : value;
  }

  private boolean defaultActive(Boolean value) {
    return value == null || value;
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysFactoryRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysWorkshopRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysLineRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysModelRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysAbnormalCategoryRecord record,
      SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysAbnormalTypeRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void fillSimpleFields(
      org.jooq.generated.tables.records.SysTeamRecord record, SysSimpleRequest request) {
    record.setName(normalizeText(request.getName()));
    record.setCode(normalizeText(request.getCode()));
    record.setSortOrder(defaultSortOrder(request.getSortOrder()));
    record.setIsActive(defaultActive(request.getIsActive()));
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void assertUpdated(int affected, String label) {
    if (affected <= 0) {
      throw new BusinessException(label + "不存在");
    }
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
