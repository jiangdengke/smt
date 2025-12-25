package org.jdk.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jdk.project.dto.user.UserCreateRequest;
import org.jdk.project.dto.user.UserUpdateRequest;
import org.jdk.project.dto.user.UserViewDto;
import org.jdk.project.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 账号管理接口（管理员）。 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

  private final UserAdminService userAdminService;

  @GetMapping
  public List<UserViewDto> listUsers() {
    return userAdminService.listUsers();
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public Long createUser(@RequestBody @Valid UserCreateRequest request) {
    return userAdminService.createUser(request);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public void updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
    userAdminService.updateUser(id, request);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable Long id) {
    userAdminService.deleteUser(id);
  }
}
