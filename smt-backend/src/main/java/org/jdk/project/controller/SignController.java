package org.jdk.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jdk.project.config.security.Jwt;
import org.jdk.project.dto.sign.SignInDto;
import org.jdk.project.service.SignService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import java.security.Principal;
import org.jdk.project.dto.sign.SignMeDto;
import java.util.List;
import org.jdk.project.dto.user.UserPasswordRequest;
import org.jdk.project.service.UserAdminService;

/** 认证接口：登录、登出。 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignController {

  private final SignService signService;

  private final Jwt jwt;

  private final UserRepository userRepository;

  private final UserAdminService userAdminService;

  /**
   * 登录并下发 JWT 到 Cookie。
   *
   * @param request HTTP 请求
   * @param response HTTP 响应
   * @param signInDto 登录参数
   */
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-in")
  void signIn(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody @Valid SignInDto signInDto) {
    jwt.makeToken(request, response, String.valueOf(signService.signIn(signInDto)));
  }

  /**
   * 登出并清除 JWT Cookie。
   *
   * @param request HTTP 请求
   * @param response HTTP 响应
   */
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-out")
  void signOut(HttpServletRequest request, HttpServletResponse response) {
    jwt.removeToken(request, response);
  }

  /**
   * 获取当前登录用户的基础信息（包含角色与权限）。
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/me")
  public SignMeDto me(Principal principal) {
    if (principal == null) return null;
    String username = principal.getName();
    if (username == null || username.isBlank()) {
      return null;
    }
    User user = userRepository.fetchOneByUsername(username);
    if (user == null) {
      return null;
    }
    Long userId = toLongId(user.getId());
    if (userId == null) {
      return null;
    }
    List<String> roles = userRepository.fetchRoleCodesByUserId(userId);
    List<String> permissions = userRepository.fetchPermissionCodesByUserId(userId);
    SignMeDto dto = new SignMeDto();
    dto.setId(userId);
    dto.setUsername(user.getUsername());
    dto.setRoles(roles);
    dto.setPermissions(permissions);
    return dto;
  }

  /** 修改本人密码。 */
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/me/password")
  public void changePassword(Principal principal, @RequestBody @Valid UserPasswordRequest request) {
    if (principal == null) {
      throw new org.jdk.project.exception.BusinessException("未登录");
    }
    Long userId = toLongId(principal.getName());
    if (userId == null) {
      throw new org.jdk.project.exception.BusinessException("用户ID无效");
    }
    userAdminService.changeOwnPassword(userId, request.getNewPassword());
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
    try {
      return Long.valueOf(text);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

}
