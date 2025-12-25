package org.jdk.project.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdk.project.dto.user.UserCreateRequest;
import org.jdk.project.dto.user.UserUpdateRequest;
import org.jdk.project.dto.user.UserViewDto;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 账号管理服务。 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

  private final UserRepository userRepository;

  public List<UserViewDto> listUsers() {
    List<User> users = userRepository.fetchAllUsers();
    List<UserViewDto> dtos = new ArrayList<>(users.size());
    for (User user : users) {
      Long userId = toLongId(user.getId());
      List<String> roles = userId == null ? List.of() : userRepository.fetchRoleCodesByUserId(userId);
      String roleCode = roles.isEmpty() ? null : roles.get(0);
      UserViewDto dto = new UserViewDto();
      dto.setId(userId);
      dto.setUsername(user.getUsername());
      dto.setRoleCode(roleCode);
      dtos.add(dto);
    }
    return dtos;
  }

  @Transactional(rollbackFor = Throwable.class)
  public Long createUser(UserCreateRequest request) {
    if (userRepository.fetchOneByUsername(request.getUsername()) != null) {
      throw new BusinessException("账号已存在");
    }
    Long roleId = userRepository.fetchRoleIdByCode(request.getRoleCode());
    if (roleId == null) {
      throw new BusinessException("角色不存在");
    }
    Long userId =
        userRepository.insertUser(StringUtils.trim(request.getUsername()), request.getPassword());
    if (userId == null) {
      throw new BusinessException("账号创建失败");
    }
    userRepository.insertUserRole(userId, roleId);
    return userId;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void updateUser(Long userId, UserUpdateRequest request) {
    User user = userRepository.fetchOneById(userId);
    if (user == null) {
      throw new BusinessException("账号不存在");
    }
    boolean changed = false;
    if (StringUtils.isNotBlank(request.getUsername())) {
      String newUsername = StringUtils.trim(request.getUsername());
      if (!newUsername.equals(user.getUsername())) {
        if (userRepository.fetchOneByUsername(newUsername) != null) {
          throw new BusinessException("账号已存在");
        }
        userRepository.updateUsername(userId, newUsername);
        changed = true;
      }
    }
    if (StringUtils.isNotBlank(request.getPassword())) {
      userRepository.updatePassword(userId, request.getPassword());
      changed = true;
    }
    if (StringUtils.isNotBlank(request.getRoleCode())) {
      Long roleId = userRepository.fetchRoleIdByCode(request.getRoleCode());
      if (roleId == null) {
        throw new BusinessException("角色不存在");
      }
      userRepository.deleteUserRoles(userId);
      userRepository.insertUserRole(userId, roleId);
      changed = true;
    }
    if (!changed) {
      throw new BusinessException("未提交有效修改");
    }
  }

  @Transactional(rollbackFor = Throwable.class)
  public void deleteUser(Long userId) {
    if (userRepository.fetchOneById(userId) == null) {
      throw new BusinessException("账号不存在");
    }
    userRepository.deleteUserRoles(userId);
    userRepository.deleteById(userId);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void changeOwnPassword(Long userId, String newPassword) {
    User user = userRepository.fetchOneById(userId);
    if (user == null) {
      throw new BusinessException("账号不存在");
    }
    userRepository.updatePassword(userId, newPassword);
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
