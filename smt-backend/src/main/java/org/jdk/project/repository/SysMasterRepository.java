package org.jdk.project.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.generated.tables.pojos.SysAbnormalCategory;
import org.jooq.generated.tables.pojos.SysAbnormalType;
import org.jooq.generated.tables.pojos.SysFactory;
import org.jooq.generated.tables.pojos.SysLine;
import org.jooq.generated.tables.pojos.SysMachine;
import org.jooq.generated.tables.pojos.SysModel;
import org.jooq.generated.tables.pojos.SysPerson;
import org.jooq.generated.tables.pojos.SysTeam;
import org.jooq.generated.tables.pojos.SysWorkshop;
import org.springframework.stereotype.Repository;

/** 系统维护表数据访问。 */
@Repository
@RequiredArgsConstructor
public class SysMasterRepository {

  private static final org.jooq.generated.tables.SysFactory SYS_FACTORY =
      org.jooq.generated.tables.SysFactory.SYS_FACTORY;
  private static final org.jooq.generated.tables.SysWorkshop SYS_WORKSHOP =
      org.jooq.generated.tables.SysWorkshop.SYS_WORKSHOP;
  private static final org.jooq.generated.tables.SysLine SYS_LINE =
      org.jooq.generated.tables.SysLine.SYS_LINE;
  private static final org.jooq.generated.tables.SysModel SYS_MODEL =
      org.jooq.generated.tables.SysModel.SYS_MODEL;
  private static final org.jooq.generated.tables.SysMachine SYS_MACHINE =
      org.jooq.generated.tables.SysMachine.SYS_MACHINE;
  private static final org.jooq.generated.tables.SysAbnormalCategory SYS_ABNORMAL_CATEGORY =
      org.jooq.generated.tables.SysAbnormalCategory.SYS_ABNORMAL_CATEGORY;
  private static final org.jooq.generated.tables.SysAbnormalType SYS_ABNORMAL_TYPE =
      org.jooq.generated.tables.SysAbnormalType.SYS_ABNORMAL_TYPE;
  private static final org.jooq.generated.tables.SysTeam SYS_TEAM =
      org.jooq.generated.tables.SysTeam.SYS_TEAM;
  private static final org.jooq.generated.tables.SysPerson SYS_PERSON =
      org.jooq.generated.tables.SysPerson.SYS_PERSON;

  private final DSLContext dsl;

  public List<SysFactory> listFactories() {
    return dsl.selectFrom(SYS_FACTORY)
        .orderBy(SYS_FACTORY.SORT_ORDER.asc(), SYS_FACTORY.ID.asc())
        .fetchInto(SysFactory.class);
  }

  public List<SysWorkshop> listWorkshops(Long factoryId) {
    Condition condition = DSL.condition("1=1");
    if (factoryId != null) {
      condition = condition.and(SYS_WORKSHOP.FACTORY_ID.eq(factoryId));
    }
    return dsl.selectFrom(SYS_WORKSHOP)
        .where(condition)
        .orderBy(SYS_WORKSHOP.SORT_ORDER.asc(), SYS_WORKSHOP.ID.asc())
        .fetchInto(SysWorkshop.class);
  }

  public List<SysLine> listLines(Long workshopId) {
    Condition condition = DSL.condition("1=1");
    if (workshopId != null) {
      condition = condition.and(SYS_LINE.WORKSHOP_ID.eq(workshopId));
    }
    return dsl.selectFrom(SYS_LINE)
        .where(condition)
        .orderBy(SYS_LINE.SORT_ORDER.asc(), SYS_LINE.ID.asc())
        .fetchInto(SysLine.class);
  }

  public List<SysModel> listModels() {
    return dsl.selectFrom(SYS_MODEL)
        .orderBy(SYS_MODEL.SORT_ORDER.asc(), SYS_MODEL.ID.asc())
        .fetchInto(SysModel.class);
  }

  public List<SysMachine> listMachines(Long modelId) {
    Condition condition = DSL.condition("1=1");
    if (modelId != null) {
      condition = condition.and(SYS_MACHINE.MODEL_ID.eq(modelId));
    }
    return dsl.selectFrom(SYS_MACHINE)
        .where(condition)
        .orderBy(SYS_MACHINE.SORT_ORDER.asc(), SYS_MACHINE.ID.asc())
        .fetchInto(SysMachine.class);
  }

  public List<SysAbnormalCategory> listAbnormalCategories() {
    return dsl.selectFrom(SYS_ABNORMAL_CATEGORY)
        .orderBy(SYS_ABNORMAL_CATEGORY.SORT_ORDER.asc(), SYS_ABNORMAL_CATEGORY.ID.asc())
        .fetchInto(SysAbnormalCategory.class);
  }

  public List<SysAbnormalType> listAbnormalTypes(Long abnormalCategoryId) {
    Condition condition = DSL.condition("1=1");
    if (abnormalCategoryId != null) {
      condition = condition.and(SYS_ABNORMAL_TYPE.ABNORMAL_CATEGORY_ID.eq(abnormalCategoryId));
    }
    return dsl.selectFrom(SYS_ABNORMAL_TYPE)
        .where(condition)
        .orderBy(SYS_ABNORMAL_TYPE.SORT_ORDER.asc(), SYS_ABNORMAL_TYPE.ID.asc())
        .fetchInto(SysAbnormalType.class);
  }

  public List<SysTeam> listTeams() {
    return dsl.selectFrom(SYS_TEAM)
        .orderBy(SYS_TEAM.SORT_ORDER.asc(), SYS_TEAM.ID.asc())
        .fetchInto(SysTeam.class);
  }

  public List<SysPerson> listPeople(Long teamId) {
    Condition condition = DSL.condition("1=1");
    if (teamId != null) {
      condition = condition.and(SYS_PERSON.TEAM_ID.eq(teamId));
    }
    return dsl.selectFrom(SYS_PERSON)
        .where(condition)
        .orderBy(SYS_PERSON.ID.asc())
        .fetchInto(SysPerson.class);
  }


  public Long insertFactory(String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_factory ([name], [code], [sort_order], [remark]) "
            + "output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, name, code, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateFactory(Long id, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_FACTORY)
        .set(SYS_FACTORY.NAME, name)
        .set(SYS_FACTORY.CODE, code)
        .set(SYS_FACTORY.SORT_ORDER, sortOrder)
        .set(SYS_FACTORY.REMARK, remark)
        .where(SYS_FACTORY.ID.eq(id))
        .execute();
  }

  public int deleteFactory(Long id) {
    return dsl.deleteFrom(SYS_FACTORY).where(SYS_FACTORY.ID.eq(id)).execute();
  }

  public boolean existsWorkshopByFactory(Long factoryId) {
    return exists(SYS_WORKSHOP, SYS_WORKSHOP.FACTORY_ID.eq(factoryId));
  }


  public Long insertWorkshop(
      Long factoryId, String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_workshop ([factory_id], [name], [code], [sort_order], "
            + "[remark]) output inserted.id values (?, ?, ?, ?, ?)";
    return dsl.resultQuery(sql, factoryId, name, code, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateWorkshop(
      Long id, Long factoryId, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_WORKSHOP)
        .set(SYS_WORKSHOP.FACTORY_ID, factoryId)
        .set(SYS_WORKSHOP.NAME, name)
        .set(SYS_WORKSHOP.CODE, code)
        .set(SYS_WORKSHOP.SORT_ORDER, sortOrder)
        .set(SYS_WORKSHOP.REMARK, remark)
        .where(SYS_WORKSHOP.ID.eq(id))
        .execute();
  }

  public int deleteWorkshop(Long id) {
    return dsl.deleteFrom(SYS_WORKSHOP).where(SYS_WORKSHOP.ID.eq(id)).execute();
  }

  public boolean existsLineByWorkshop(Long workshopId) {
    return exists(SYS_LINE, SYS_LINE.WORKSHOP_ID.eq(workshopId));
  }


  public Long insertLine(
      Long workshopId, String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_line ([workshop_id], [name], [code], [sort_order], "
            + "[remark]) output inserted.id values (?, ?, ?, ?, ?)";
    return dsl.resultQuery(sql, workshopId, name, code, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateLine(
      Long id, Long workshopId, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_LINE)
        .set(SYS_LINE.WORKSHOP_ID, workshopId)
        .set(SYS_LINE.NAME, name)
        .set(SYS_LINE.CODE, code)
        .set(SYS_LINE.SORT_ORDER, sortOrder)
        .set(SYS_LINE.REMARK, remark)
        .where(SYS_LINE.ID.eq(id))
        .execute();
  }

  public int deleteLine(Long id) {
    return dsl.deleteFrom(SYS_LINE).where(SYS_LINE.ID.eq(id)).execute();
  }


  public Long insertModel(String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_model ([name], [code], [sort_order], [remark]) "
            + "output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, name, code, sortOrder, remark).fetchOne(0, Long.class);
  }

  public int updateModel(Long id, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_MODEL)
        .set(SYS_MODEL.NAME, name)
        .set(SYS_MODEL.CODE, code)
        .set(SYS_MODEL.SORT_ORDER, sortOrder)
        .set(SYS_MODEL.REMARK, remark)
        .where(SYS_MODEL.ID.eq(id))
        .execute();
  }

  public int deleteModel(Long id) {
    return dsl.deleteFrom(SYS_MODEL).where(SYS_MODEL.ID.eq(id)).execute();
  }

  public boolean existsMachineByModel(Long modelId) {
    return exists(SYS_MACHINE, SYS_MACHINE.MODEL_ID.eq(modelId));
  }


  public Long insertMachine(
      Long modelId, String machineNo, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_machine ([model_id], [machine_no], [sort_order], "
            + "[remark]) output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, modelId, machineNo, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateMachine(
      Long id, Long modelId, String machineNo, int sortOrder, String remark) {
    return dsl.update(SYS_MACHINE)
        .set(SYS_MACHINE.MODEL_ID, modelId)
        .set(SYS_MACHINE.MACHINE_NO, machineNo)
        .set(SYS_MACHINE.SORT_ORDER, sortOrder)
        .set(SYS_MACHINE.REMARK, remark)
        .where(SYS_MACHINE.ID.eq(id))
        .execute();
  }

  public int deleteMachine(Long id) {
    return dsl.deleteFrom(SYS_MACHINE).where(SYS_MACHINE.ID.eq(id)).execute();
  }


  public Long insertAbnormalCategory(String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_abnormal_category ([name], [code], [sort_order], [remark]) "
            + "output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, name, code, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateAbnormalCategory(
      Long id, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_ABNORMAL_CATEGORY)
        .set(SYS_ABNORMAL_CATEGORY.NAME, name)
        .set(SYS_ABNORMAL_CATEGORY.CODE, code)
        .set(SYS_ABNORMAL_CATEGORY.SORT_ORDER, sortOrder)
        .set(SYS_ABNORMAL_CATEGORY.REMARK, remark)
        .where(SYS_ABNORMAL_CATEGORY.ID.eq(id))
        .execute();
  }

  public int deleteAbnormalCategory(Long id) {
    return dsl.deleteFrom(SYS_ABNORMAL_CATEGORY)
        .where(SYS_ABNORMAL_CATEGORY.ID.eq(id))
        .execute();
  }

  public boolean existsAbnormalTypeByCategory(Long abnormalCategoryId) {
    return exists(SYS_ABNORMAL_TYPE, SYS_ABNORMAL_TYPE.ABNORMAL_CATEGORY_ID.eq(abnormalCategoryId));
  }


  public Long insertAbnormalType(
      Long abnormalCategoryId,
      String name,
      String code,
      int sortOrder,
      String remark) {
    String sql =
        "insert into smtBackend.sys_abnormal_type ([abnormal_category_id], [name], [code], "
            + "[sort_order], [remark]) output inserted.id values (?, ?, ?, ?, ?)";
    return dsl.resultQuery(sql, abnormalCategoryId, name, code, sortOrder, remark)
        .fetchOne(0, Long.class);
  }

  public int updateAbnormalType(
      Long id,
      Long abnormalCategoryId,
      String name,
      String code,
      int sortOrder,
      String remark) {
    return dsl.update(SYS_ABNORMAL_TYPE)
        .set(SYS_ABNORMAL_TYPE.ABNORMAL_CATEGORY_ID, abnormalCategoryId)
        .set(SYS_ABNORMAL_TYPE.NAME, name)
        .set(SYS_ABNORMAL_TYPE.CODE, code)
        .set(SYS_ABNORMAL_TYPE.SORT_ORDER, sortOrder)
        .set(SYS_ABNORMAL_TYPE.REMARK, remark)
        .where(SYS_ABNORMAL_TYPE.ID.eq(id))
        .execute();
  }

  public int deleteAbnormalType(Long id) {
    return dsl.deleteFrom(SYS_ABNORMAL_TYPE).where(SYS_ABNORMAL_TYPE.ID.eq(id)).execute();
  }


  public Long insertTeam(String name, String code, int sortOrder, String remark) {
    String sql =
        "insert into smtBackend.sys_team ([name], [code], [sort_order], [remark]) "
            + "output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, name, code, sortOrder, remark).fetchOne(0, Long.class);
  }

  public int updateTeam(Long id, String name, String code, int sortOrder, String remark) {
    return dsl.update(SYS_TEAM)
        .set(SYS_TEAM.NAME, name)
        .set(SYS_TEAM.CODE, code)
        .set(SYS_TEAM.SORT_ORDER, sortOrder)
        .set(SYS_TEAM.REMARK, remark)
        .where(SYS_TEAM.ID.eq(id))
        .execute();
  }

  public int deleteTeam(Long id) {
    return dsl.deleteFrom(SYS_TEAM).where(SYS_TEAM.ID.eq(id)).execute();
  }

  public boolean existsPersonByTeam(Long teamId) {
    return exists(SYS_PERSON, SYS_PERSON.TEAM_ID.eq(teamId));
  }


  public Long insertPerson(
      Long teamId, String name, String employeeNo, String remark) {
    String sql =
        "insert into smtBackend.sys_person ([team_id], [name], [employee_no], [remark]) "
            + "output inserted.id values (?, ?, ?, ?)";
    return dsl.resultQuery(sql, teamId, name, employeeNo, remark)
        .fetchOne(0, Long.class);
  }

  public int updatePerson(
      Long id, Long teamId, String name, String employeeNo, String remark) {
    return dsl.update(SYS_PERSON)
        .set(SYS_PERSON.TEAM_ID, teamId)
        .set(SYS_PERSON.NAME, name)
        .set(SYS_PERSON.EMPLOYEE_NO, employeeNo)
        .set(SYS_PERSON.REMARK, remark)
        .where(SYS_PERSON.ID.eq(id))
        .execute();
  }

  public int deletePerson(Long id) {
    return dsl.deleteFrom(SYS_PERSON).where(SYS_PERSON.ID.eq(id)).execute();
  }



  private Long fetchIdentity() {
    return dsl.select(DSL.field("cast(scope_identity() as bigint)", Long.class))
        .fetchOne(0, Long.class);
  }

  private boolean exists(Table<?> table, Condition condition) {
    return dsl.selectCount().from(table).where(condition).fetchOne(0, int.class) > 0;
  }
}
