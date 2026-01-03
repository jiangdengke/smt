package org.jdk.project.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.sys.*;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.SysMasterRepository;
import org.jooq.generated.tables.pojos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 系统维护表服务。 */
@Service
@RequiredArgsConstructor
public class SysMasterService {

  private final SysMasterRepository sysMasterRepository;

  public List<SysFactory> listFactories() {
    return sysMasterRepository.listFactories();
  }

  public List<SysWorkshop> listWorkshops(Long factoryId) {
    return sysMasterRepository.listWorkshops(factoryId);
  }

  public List<SysLine> listLines(Long workshopId) {
    return sysMasterRepository.listLines(workshopId);
  }

  public List<SysMachine> listMachines(Long lineId) {
    return sysMasterRepository.listMachines(lineId);
  }

  public List<SysAbnormalCategory> listAbnormalCategories() {
    return sysMasterRepository.listAbnormalCategories();
  }

  public List<SysAbnormalType> listAbnormalTypes(Long abnormalCategoryId) {
    return sysMasterRepository.listAbnormalTypes(abnormalCategoryId);
  }

  public List<SysTeam> listTeams() {
    return sysMasterRepository.listTeams();
  }

  public List<SysPerson> listPeople(Long teamId) {
    return sysMasterRepository.listPeople(teamId);
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createFactory(SysSimpleRequest request) {
    Long id =
        sysMasterRepository.insertFactory(
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("厂区创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateFactory(Long id, SysSimpleRequest request) {
    int updated =
        sysMasterRepository.updateFactory(
            id,
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "厂区");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteFactory(Long id) {
    if (sysMasterRepository.existsWorkshopByFactory(id)) {
      throw new BusinessException("厂区下存在车间，无法删除");
    }
    assertUpdated(sysMasterRepository.deleteFactory(id), "厂区");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createWorkshop(SysWorkshopRequest request) {
    Long id =
        sysMasterRepository.insertWorkshop(
            request.getFactoryId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("车间创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateWorkshop(Long id, SysWorkshopRequest request) {
    int updated =
        sysMasterRepository.updateWorkshop(
            id,
            request.getFactoryId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "车间");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteWorkshop(Long id) {
    if (sysMasterRepository.existsLineByWorkshop(id)) {
      throw new BusinessException("车间下存在线别，无法删除");
    }
    assertUpdated(sysMasterRepository.deleteWorkshop(id), "车间");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createLine(SysLineRequest request) {
    Long id =
        sysMasterRepository.insertLine(
            request.getWorkshopId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("线别创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateLine(Long id, SysLineRequest request) {
    int updated =
        sysMasterRepository.updateLine(
            id,
            request.getWorkshopId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "线别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteLine(Long id) {
    if (sysMasterRepository.existsMachineByLine(id)) {
      throw new BusinessException("线别下存在机台，无法删除");
    }
    assertUpdated(sysMasterRepository.deleteLine(id), "线别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createMachine(SysMachineRequest request) {
    Long id =
        sysMasterRepository.insertMachine(
            request.getLineId(),
            normalizeText(request.getMachineNo()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("机台创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateMachine(Long id, SysMachineRequest request) {
    int updated =
        sysMasterRepository.updateMachine(
            id,
            request.getLineId(),
            normalizeText(request.getMachineNo()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "机台");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteMachine(Long id) {
    assertUpdated(sysMasterRepository.deleteMachine(id), "机台");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createAbnormalCategory(SysSimpleRequest request) {
    Long id =
        sysMasterRepository.insertAbnormalCategory(
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("异常类别创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateAbnormalCategory(Long id, SysSimpleRequest request) {
    int updated =
        sysMasterRepository.updateAbnormalCategory(
            id,
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "异常类别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteAbnormalCategory(Long id) {
    if (sysMasterRepository.existsAbnormalTypeByCategory(id)) {
      throw new BusinessException("异常类别下存在异常分类，无法删除");
    }
    assertUpdated(sysMasterRepository.deleteAbnormalCategory(id), "异常类别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createAbnormalType(SysAbnormalTypeRequest request) {
    Long id =
        sysMasterRepository.insertAbnormalType(
            request.getAbnormalCategoryId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("异常分类创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateAbnormalType(Long id, SysAbnormalTypeRequest request) {
    int updated =
        sysMasterRepository.updateAbnormalType(
            id,
            request.getAbnormalCategoryId(),
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "异常分类");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteAbnormalType(Long id) {
    assertUpdated(sysMasterRepository.deleteAbnormalType(id), "异常分类");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createTeam(SysSimpleRequest request) {
    Long id =
        sysMasterRepository.insertTeam(
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("组别创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateTeam(Long id, SysSimpleRequest request) {
    int updated =
        sysMasterRepository.updateTeam(
            id,
            normalizeText(request.getName()),
            normalizeText(request.getCode()),
            defaultSortOrder(request.getSortOrder()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "组别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteTeam(Long id) {
    if (sysMasterRepository.existsPersonByTeam(id)) {
      throw new BusinessException("组别下存在人员，无法删除");
    }
    assertUpdated(sysMasterRepository.deleteTeam(id), "组别");
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createPerson(SysPersonRequest request) {
    Long id =
        sysMasterRepository.insertPerson(
            request.getTeamId(),
            normalizeText(request.getName()),
            normalizeText(request.getEmployeeNo()),
            normalizeText(request.getRemark()));
    if (id == null) {
      throw new BusinessException("人员创建失败");
    }
    return id;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updatePerson(Long id, SysPersonRequest request) {
    int updated =
        sysMasterRepository.updatePerson(
            id,
            request.getTeamId(),
            normalizeText(request.getName()),
            normalizeText(request.getEmployeeNo()),
            normalizeText(request.getRemark()));
    assertUpdated(updated, "人员");
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deletePerson(Long id) {
    assertUpdated(sysMasterRepository.deletePerson(id), "人员");
  }

  private String normalizeText(String value) {
    return StringUtils.trimToNull(value);
  }

  private int defaultSortOrder(Integer value) {
    return value == null ? 0 : value;
  }

  private void assertUpdated(int affected, String label) {
    if (affected <= 0) {
      throw new BusinessException(label + "不存在");
    }
  }
}
