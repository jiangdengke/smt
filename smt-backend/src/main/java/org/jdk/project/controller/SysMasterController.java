package org.jdk.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.sys.*;
import org.jdk.project.service.SysMasterService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 系统维护表接口。 */
@RestController
@RequestMapping("/sys")
@RequiredArgsConstructor
public class SysMasterController {

  private final SysMasterService sysMasterService;

  @GetMapping("/factories")
  public List<org.jooq.generated.tables.pojos.SysFactory> listFactories() {
    return sysMasterService.listFactories();
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/factories")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createFactory(@RequestBody @Valid SysSimpleRequest request) {
    return sysMasterService.createFactory(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/factories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateFactory(@PathVariable Long id, @RequestBody @Valid SysSimpleRequest request) {
    sysMasterService.updateFactory(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/factories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteFactory(@PathVariable Long id) {
    sysMasterService.deleteFactory(id);
  }

  @GetMapping("/workshops")
  public List<org.jooq.generated.tables.pojos.SysWorkshop> listWorkshops(
      @RequestParam(required = false) Long factoryId) {
    return sysMasterService.listWorkshops(factoryId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/workshops")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createWorkshop(@RequestBody @Valid SysWorkshopRequest request) {
    return sysMasterService.createWorkshop(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/workshops/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateWorkshop(
      @PathVariable Long id, @RequestBody @Valid SysWorkshopRequest request) {
    sysMasterService.updateWorkshop(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/workshops/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteWorkshop(@PathVariable Long id) {
    sysMasterService.deleteWorkshop(id);
  }

  @GetMapping("/lines")
  public List<org.jooq.generated.tables.pojos.SysLine> listLines(
      @RequestParam(required = false) Long workshopId) {
    return sysMasterService.listLines(workshopId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/lines")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createLine(@RequestBody @Valid SysLineRequest request) {
    return sysMasterService.createLine(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/lines/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateLine(@PathVariable Long id, @RequestBody @Valid SysLineRequest request) {
    sysMasterService.updateLine(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/lines/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteLine(@PathVariable Long id) {
    sysMasterService.deleteLine(id);
  }

  @GetMapping("/machines")
  public List<org.jooq.generated.tables.pojos.SysMachine> listMachines(
      @RequestParam(required = false) Long lineId) {
    return sysMasterService.listMachines(lineId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/machines")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createMachine(@RequestBody @Valid SysMachineRequest request) {
    return sysMasterService.createMachine(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/machines/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateMachine(@PathVariable Long id, @RequestBody @Valid SysMachineRequest request) {
    sysMasterService.updateMachine(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/machines/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteMachine(@PathVariable Long id) {
    sysMasterService.deleteMachine(id);
  }

  @GetMapping("/abnormal-categories")
  public List<org.jooq.generated.tables.pojos.SysAbnormalCategory> listAbnormalCategories() {
    return sysMasterService.listAbnormalCategories();
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/abnormal-categories")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createAbnormalCategory(@RequestBody @Valid SysSimpleRequest request) {
    return sysMasterService.createAbnormalCategory(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/abnormal-categories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateAbnormalCategory(
      @PathVariable Long id, @RequestBody @Valid SysSimpleRequest request) {
    sysMasterService.updateAbnormalCategory(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/abnormal-categories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteAbnormalCategory(@PathVariable Long id) {
    sysMasterService.deleteAbnormalCategory(id);
  }

  @GetMapping("/abnormal-types")
  public List<org.jooq.generated.tables.pojos.SysAbnormalType> listAbnormalTypes(
      @RequestParam(required = false) Long abnormalCategoryId) {
    return sysMasterService.listAbnormalTypes(abnormalCategoryId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/abnormal-types")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createAbnormalType(@RequestBody @Valid SysAbnormalTypeRequest request) {
    return sysMasterService.createAbnormalType(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/abnormal-types/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateAbnormalType(
      @PathVariable Long id, @RequestBody @Valid SysAbnormalTypeRequest request) {
    sysMasterService.updateAbnormalType(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/abnormal-types/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteAbnormalType(@PathVariable Long id) {
    sysMasterService.deleteAbnormalType(id);
  }

  @GetMapping("/teams")
  public List<org.jooq.generated.tables.pojos.SysTeam> listTeams() {
    return sysMasterService.listTeams();
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/teams")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createTeam(@RequestBody @Valid SysSimpleRequest request) {
    return sysMasterService.createTeam(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/teams/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updateTeam(@PathVariable Long id, @RequestBody @Valid SysSimpleRequest request) {
    sysMasterService.updateTeam(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/teams/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteTeam(@PathVariable Long id) {
    sysMasterService.deleteTeam(id);
  }

  @GetMapping("/people")
  public List<org.jooq.generated.tables.pojos.SysPerson> listPeople(
      @RequestParam(required = false) Long teamId) {
    return sysMasterService.listPeople(teamId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/people")
  @PreAuthorize("hasRole('ADMIN')")
  public Long createPerson(@RequestBody @Valid SysPersonRequest request) {
    return sysMasterService.createPerson(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/people/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void updatePerson(@PathVariable Long id, @RequestBody @Valid SysPersonRequest request) {
    sysMasterService.updatePerson(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/people/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deletePerson(@PathVariable Long id) {
    sysMasterService.deletePerson(id);
  }
}
