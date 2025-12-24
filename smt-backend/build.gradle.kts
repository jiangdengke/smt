// Spring Boot 打包任务类型，用于设置最终可执行 jar 名称
import org.springframework.boot.gradle.tasks.bundling.BootJar

// 统一管理 jOOQ 版本
val jooqVersion by extra("3.19.24")

plugins {
    // Java 支持
    java
    `java-library`
    // 覆盖率
    jacoco
    // Spring Boot 插件与依赖管理
    id("org.springframework.boot") version "3.3.9"
    id("io.spring.dependency-management") version "1.1.7"
    // OpenAPI 运行时仅需依赖，不需要 Gradle 插件
    // OpenAPI Gradle 插件（用于生成离线文档）
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    // jOOQ 代码生成
    id("org.jooq.jooq-codegen-gradle") version "3.19.24"
    // 代码格式化
    id("com.diffplug.spotless") version "7.1.0"
}

// 将 jOOQ 生成代码加入源码目录，便于编译
sourceSets {
    main {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
    test {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
}

group = "org.jdk.project"
version = "1.0.0"
description = "脚手架"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { url = uri("https://maven.aliyun.com/repository/apache-snapshots") }
    mavenCentral()
}

dependencies {
    // Spring Boot 基础能力
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // 工具库
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    // OpenAPI UI（与 Spring Boot 3.3 兼容的稳定版本）
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    // jOOQ 元数据支持（代码生成）
    implementation("org.jooq:jooq-meta:$jooqVersion")
    // JWT
    implementation("com.auth0:java-jwt:4.4.0")


    // 运行时依赖
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11")
    // 编译期
    compileOnly("org.projectlombok:lombok")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    // developmentOnly("org.springframework.boot:spring-boot-devtools") // 避免 DevTools 类加载干扰

    // 测试（本项目已移除测试代码，如需测试可按需添加依赖）
    // jOOQ 代码生成依赖
    jooqCodegen("com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11")
    jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
    // 配置提示生成
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    api("org.jspecify:jspecify:1.0.0")
}

// 配置 OpenAPI 生成任务（命中运行中的应用 http://localhost:8080/v3/api-docs）
openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("${project.buildDir}/openapi"))
    outputFileName.set("openapi.json")
}

// 兼容命令别名：generateOpenApiDoc -> generateOpenApiDocs
tasks.register("generateOpenApiDoc") {
    dependsOn("generateOpenApiDocs")
}

// 配置 BootJar 名称
tasks.withType<BootJar> {
    archiveFileName.set("jiangdk.jar")
}

// 使用 JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
}

// 生成测试覆盖率报告
tasks.test {
    finalizedBy(tasks.jacocoTestReport) // 测试后生成 Jacoco 报告
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // 生成报告前需要执行测试
}

jacoco {
    toolVersion = "0.8.13"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

// 统一代码风格
spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle.kts", "*.md", ".gitignore")
        // define the steps to apply to those files
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    java {
        // Google Java 格式
        googleJavaFormat("1.28.0").reflowLongStrings()
        formatAnnotations()
    }

    kotlinGradle {
        target("*.gradle.kts") // 默认作用于根目录下的 .gradle.kts
        ktlint() // 也可选 ktfmt()/prettier()
    }
}

// jOOQ 代码生成（通过 JDBC 元数据生成 POJO/DAO/Record，避免 DDL 解析差异）
jooq {
    configuration {
        jdbc {
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            url =
                "jdbc:sqlserver://localhost:1433;databaseName=project;encrypt=false;trustServerCertificate=true"
            user = "sa"
            password = "YourStrong!Passw0rd"
        }
        generator {
            database {
                includes = ".*"
                excludes = "qrtz_.*"
                name = "org.jooq.meta.jdbc.JDBCDatabase"
                inputSchema = "smtBackend"
                forcedTypes {
                    forcedType {
                        isJsonConverter = true
                        includeTypes = "(?i:JSON|JSONB)"
                    }
                    forcedType {
                        name = "Boolean" // tinyint(1) -> Boolean
                        includeExpression = ".*"
                        includeTypes = "(?i:TINYINT\\(1\\))"
                    }
                    forcedType {
                        name = "OffsetDateTime" // TIMESTAMP -> OffsetDateTime
                        includeExpression = ".*"
                        includeTypes = "TIMESTAMP"
                    }
                }
            }
            generate {
                isDaos = true // 生成 DAO
                isRecords = true // 生成 Record
                isDeprecated = false
                isImmutablePojos = false
                isFluentSetters = true
                isSpringAnnotations = true
                isSpringDao = true
            }
            target {
                packageName = "org.jooq.generated" // 生成代码包名
            }
        }
    }
}
