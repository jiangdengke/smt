package org.jdk.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jdk.project.config.security.Jwt;
import org.jdk.project.dto.sign.SignInDto;
import org.jdk.project.dto.sign.SignUpDto;
import org.jdk.project.service.SignService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.jdk.project.repository.UserRepository;
import org.jooq.generated.tables.pojos.User;
import java.security.Principal;

/** 认证接口：登录、注册、登出。 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignController {

  private final SignService signService;

  private final Jwt jwt;

  private final UserRepository userRepository;

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
   * 注册新用户。
   *
   * @param signUpDto 注册参数
   */
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/sign-up")
  void signUp(@RequestBody @Valid SignUpDto signUpDto) {
    signService.signUp(signUpDto);
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
   * 获取当前登录用户的基础信息（无角色与权限）。
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/me")
  public User me(Principal principal) {
    if (principal == null) return null;
    User user = userRepository.fetchOneByUsername(principal.getName());
    if (user != null) {
      user.setPassword(null);
    }
    return user;
  }

}
