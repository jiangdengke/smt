package org.jdk.project.dto;

import jakarta.annotation.Nullable;
import lombok.*;

/**
 * 分页响应 DTO。
 *
 * @param <T> 数据类型
 */
@Data
public class PageResponseDto<T> {
  private long total;
  private T data;

  /**
   * 构造方法。
   *
   * @param total 总条数
   * @param data 当前页数据
   */
  public PageResponseDto(long total, @Nullable T data) {
    if (total < 0) {
      throw new IllegalArgumentException("total must not be less than zero");
    }
    this.total = total;
    this.data = data;
  }

  /** 返回一个空分页结果。 */
  public static <T> PageResponseDto<T> empty() {
    return new PageResponseDto<>(0, null);
  }
}
