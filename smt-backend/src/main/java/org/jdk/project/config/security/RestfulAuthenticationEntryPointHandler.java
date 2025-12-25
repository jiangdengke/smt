package org.jdk.project.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 认证/鉴权失败的 REST 风格响应处理。
 *
 * <p>- 未登录：返回 401； - 权限不足：返回 403。
 */
public class RestfulAuthenticationEntryPointHandler
    implements AccessDeniedHandler, AuthenticationEntryPoint {

  /** 未认证处理：返回 401。 */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  /** 无权限处理：返回 403。 */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
  }
}
