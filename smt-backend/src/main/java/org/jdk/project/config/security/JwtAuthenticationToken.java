package org.jdk.project.config.security;

import java.io.Serial;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

/** 基于 JWT 的认证令牌。 */
@Setter
@Getter
@ToString
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  @Serial private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  private final Object principal;

  private String credentials;

  /** 未认证的 Token 构造（无权限）。 */
  public JwtAuthenticationToken(Object principal, String credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(false);
  }

  /** 已认证的 Token 构造（含权限）。 */
  public JwtAuthenticationToken(
      Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  /** 工厂方法：未认证。 */
  public static JwtAuthenticationToken unauthenticated(String userIdentify, String token) {
    return new JwtAuthenticationToken(userIdentify, token);
  }

  /** 工厂方法：已认证。 */
  public static JwtAuthenticationToken authenticated(
      UserDetails principal, String token, Collection<? extends GrantedAuthority> authorities) {
    return new JwtAuthenticationToken(principal, token, authorities);
  }

  /** 获取凭证（JWT 字符串）。 */
  @Override
  public String getCredentials() {
    return this.credentials;
  }

  /** 获取主体（UserDetails 或用户标识）。 */
  @Override
  public Object getPrincipal() {
    return this.principal;
  }
}
