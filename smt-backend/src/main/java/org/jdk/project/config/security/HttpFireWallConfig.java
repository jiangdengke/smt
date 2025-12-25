package org.jdk.project.config.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/** HTTP 防火墙与拒绝策略配置。 */
@Configuration
public class HttpFireWallConfig {

  /** 使用严格防火墙策略。 */
  @Bean
  public HttpFirewall getHttpFirewall() {
    return new StrictHttpFirewall();
  }

  /** 自定义请求被拒绝时的响应处理（400 文本）。 */
  @Bean
  public RequestRejectedHandler requestRejectedHandler() {
    return (request, response, requestRejectedException) -> {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      try (PrintWriter writer = response.getWriter()) {
        writer.write(requestRejectedException.getMessage());
      }
    };
  }
}
