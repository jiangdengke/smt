package org.jdk.project.utils;

/** 字符串大小写与命名格式工具。 */
public class StringCaseUtils {
  /**
   * 将驼峰命名转换为下划线命名。
   *
   * @param input 输入的驼峰字符串
   * @return 下划线格式字符串
   */
  public static String convertCamelCaseToSnake(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isUpperCase(c)) {
        result.append("_").append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
}
