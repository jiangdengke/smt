package org.jdk.project.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * JWT 工具组件。
 *
 * <p>- 负责创建、校验、解析 JWT； - 将 JWT 写入/移除到 HTTP Cookie 中。
 */
@Slf4j
@Component
@Getter
public class Jwt {

  private final String secret;

  private final int expirationMin;

  private final String cookieName;

  private final JWTVerifier verifier;

  /**
   * 通过配置构造 JWT 校验器。
   *
   * @param secret 签名密钥
   * @param expirationMin 过期时间，单位：分钟
   * @param cookieName Cookie 名称
   */
  public Jwt(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-min}") int expirationMin,
      @Value("${jwt.cookie-name}") String cookieName) {
    this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
    this.secret = secret;
    this.expirationMin = expirationMin;
    this.cookieName = cookieName;
  }

  /**
   * 提取 JWT 的 subject（通常为用户标识）。
   *
   * @param token JWT 字符串
   * @return subject
   */
  public String getSubject(String token) {
    return JWT.decode(token).getSubject();
  }

  /**
   * 校验 JWT 是否有效。
   *
   * @param token JWT 字符串
   * @return 是否通过校验
   */
  public Boolean verify(String token) {
    try {
      verifier.verify(token);
      return Boolean.TRUE;
    } catch (JWTVerificationException e) {
      return Boolean.FALSE;
    }
  }

  /**
   * 从请求 Cookie 中提取 JWT。
   *
   * @param request HTTP 请求
   * @return JWT 字符串，若不存在返回 null
   */
  public String extract(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  /**
   * 生成一个 JWT。
   *
   * @param userIdentify 用户标识（写入 subject）
   * @return JWT 字符串
   */
  public String create(String userIdentify) {
    return JWT.create()
        .withSubject(String.valueOf(userIdentify))
        .withIssuedAt(new Date())
        .withExpiresAt(
            Date.from(
                LocalDateTime.now()
                    .plusMinutes(expirationMin)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()))
        .sign(Algorithm.HMAC256(secret));
  }

  /**
   * 构建携带 JWT 的 Cookie。
   *
   * @param request HTTP 请求（用于推断 Cookie Path 与是否 Secure）
   * @param userIdentify 用户标识
   * @return Cookie 对象
   */
  private Cookie buildJwtCookiePojo(HttpServletRequest request, String userIdentify) {
    String contextPath = request.getContextPath();
    String cookiePath = StringUtils.isNotEmpty(contextPath) ? contextPath : "/";
    Cookie cookie = new Cookie(cookieName, create(userIdentify));
    cookie.setPath(cookiePath);
    cookie.setMaxAge(expirationMin * 60);
    cookie.setSecure(request.isSecure());
    cookie.setHttpOnly(true);
    return cookie;
  }

  /**
   * 将 JWT 写入响应 Cookie。
   *
   * @param request HTTP 请求
   * @param response HTTP 响应
   * @param userIdentify 用户标识
   */
  public void makeToken(
      HttpServletRequest request, HttpServletResponse response, String userIdentify) {
    response.addCookie(buildJwtCookiePojo(request, userIdentify));
  }

  /**
   * 从响应中移除 JWT Cookie（通过设置 MaxAge=0）。
   *
   * @param request HTTP 请求
   * @param response HTTP 响应
   */
  public void removeToken(HttpServletRequest request, HttpServletResponse response) {
    String contextPath = request.getContextPath();
    String cookiePath = StringUtils.isNotEmpty(contextPath) ? contextPath : "/";
    Cookie expired = new Cookie(cookieName, "");
    expired.setPath(cookiePath);
    expired.setMaxAge(0);
    expired.setHttpOnly(true);
    expired.setSecure(request.isSecure());
    // 如有自定义 Domain，这里也需要保持一致
    response.addCookie(expired);
  }
}
