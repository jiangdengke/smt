package org.jdk.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdk.project.dto.sign.SignInDto;
import org.jdk.project.dto.sign.SignUpDto;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 登录注册相关业务逻辑。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignService {

  private static final String DEFAULT_ROLE_CODE = "USER";

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  /**
   * 用户登录。
   *
   * @param signInDto 登录请求（用户名/密码）
   * @return 登录成功后的用户ID
   */
  public Long signIn(SignInDto signInDto) {
    User user = userRepository.fetchOneByUsername(signInDto.getUsername());
    if (user == null) {
      throw new BusinessException(String.format("%s user not found", signInDto.getUsername()));
    }
    if (!passwordEncoder.matches(signInDto.getPassword(), user.getPassword())) {
      throw new BusinessException("password invalid");
    }
    Long userId = toLongId(user.getId());
    if (userId == null) {
      throw new BusinessException("用户ID无效");
    }
    return userId;
  }

  /**
   * 用户注册，并绑定默认角色。
   *
   * @param signUpDto 注册请求（用户名/密码）
   */
  @Transactional(rollbackFor = Throwable.class)
  public void signUp(SignUpDto signUpDto) {
    // 用户名唯一性校验
    if (userRepository.fetchOneByUsername(signUpDto.getUsername()) != null) {
      throw new BusinessException(
          String.format("username %s already exist", signUpDto.getUsername()));
    }
    User user = new User();
    user.setUsername(signUpDto.getUsername());
    user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
    userRepository.insert(user);
    Long userId = toLongId(user.getId());
    if (userId == null) {
      User saved = userRepository.fetchOneByUsername(signUpDto.getUsername());
      userId = saved == null ? null : toLongId(saved.getId());
    }
    Long roleId = userRepository.fetchRoleIdByCode(DEFAULT_ROLE_CODE);
    if (userId == null || roleId == null) {
      throw new BusinessException("默认角色不存在");
    }
    userRepository.insertUserRole(userId, roleId);
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
