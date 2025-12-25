package org.jdk.project.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.*;

/**
 * Spring Security 配置。
 *
 * <p>- 定义公开接口、鉴权规则； - 定义密码编码器、认证管理器； - 注册 JWT 过滤器实现无状态会话。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;

  private final Jwt jwt;


  /** 认证管理器。 */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /** 白名单接口匹配器。 */
  @Bean
  public RequestMatcher publicEndPointMatcher() {
    return new OrRequestMatcher(
        new AntPathRequestMatcher("/auth/sign-in", HttpMethod.POST.name()),
        new AntPathRequestMatcher("/v3/api-docs/**", HttpMethod.GET.name()),
        new AntPathRequestMatcher("/swagger-ui/**", HttpMethod.GET.name()),
        new AntPathRequestMatcher("/swagger-ui.html", HttpMethod.GET.name()),
        new AntPathRequestMatcher("/error"));
  }

  /** 安全过滤器链配置。 */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    RestfulAuthenticationEntryPointHandler restfulAuthenticationEntryPointHandler =
        new RestfulAuthenticationEntryPointHandler();
    /*
    <Stateless API CSRF protection>
    http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
    */
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(publicEndPointMatcher())
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            (exceptionHandling) ->
                exceptionHandling
                    .accessDeniedHandler(restfulAuthenticationEntryPointHandler)
                    .authenticationEntryPoint(restfulAuthenticationEntryPointHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAt(
            new JwtAuthenticationFilter(jwt, userDetailsService),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
