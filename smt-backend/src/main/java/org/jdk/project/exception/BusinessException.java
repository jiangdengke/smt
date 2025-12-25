package org.jdk.project.exception;

/** 业务异常。 */
public class BusinessException extends RuntimeException {

  @java.io.Serial private static final long serialVersionUID = -2119302295305964305L;

  /** 无参构造。 */
  public BusinessException() {}

  /** 指定消息的构造。 */
  public BusinessException(String message) {
    super(message);
  }

  /** 指定消息与原因的构造。 */
  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }

  /** 指定原因的构造。 */
  public BusinessException(Throwable cause) {
    super(cause);
  }

  /** 完整参数构造。 */
  public BusinessException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
