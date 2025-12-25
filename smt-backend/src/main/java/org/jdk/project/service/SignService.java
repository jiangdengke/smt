package org.jdk.project.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdk.project.dto.sign.SignInDto;
import org.jdk.project.exception.BusinessException;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import org.springframework.stereotype.Service;

/** 登录相关业务逻辑。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignService {

  private final UserRepository userRepository;

  /**
   * 用户登录。
   *
   * @param signInDto 登录请求（用户名/密码）
   * @return 登录成功后的用户ID
   */
  public Long signIn(SignInDto signInDto) {
    User user = userRepository.fetchOneByUsername(signInDto.getUsername());
    if (user == null) {
      throw new BusinessException("用户名或密码错误");
    }
    if (!Objects.equals(signInDto.getPassword(), user.getPassword())) {
      throw new BusinessException("用户名或密码错误");
    }
    Long userId = toLongId(user.getId());
    if (userId == null) {
      throw new BusinessException("用户ID无效");
    }
    return userId;
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
