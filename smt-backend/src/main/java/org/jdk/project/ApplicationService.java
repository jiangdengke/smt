package org.jdk.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口类。
 *
 * <p>- 负责启动 Spring Boot 应用； - 指定扫描包，包含业务包与 jOOQ 代码生成包。
 */
@SpringBootApplication(scanBasePackages = {"org.jdk.project", "org.jooq.generated"})
public class ApplicationService {

  /**
   * 启动方法。
   *
   * @param args 启动参数
   */
  public static void main(String[] args) {
    SpringApplication.run(ApplicationService.class, args);
  }
}
