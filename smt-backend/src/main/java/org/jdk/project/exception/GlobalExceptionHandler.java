package org.jdk.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 全局异常处理器。
 *
 * <p>统一处理业务异常、参数校验异常、请求拒绝等，返回标准的 ProblemDetail 响应体。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  /** 业务异常处理。 */
  @ExceptionHandler(value = {BusinessException.class})
  public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
    log.error("Business Error Handled  ===> ", ex);
    ErrorResponseException errorResponseException =
        new ErrorResponseException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()),
            ex.getCause());
    return handleExceptionInternal(
        errorResponseException,
        errorResponseException.getBody(),
        errorResponseException.getHeaders(),
        errorResponseException.getStatusCode(),
        request);
  }

  /** 参数校验失败处理（@Valid）。 */
  @SuppressWarnings("NullableProblems")
  @Override
  @Nullable public ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    log.error("MethodArgumentNotValidException Handled  ===> ", ex);
    ErrorResponseException errorResponseException =
        new ErrorResponseException(
            status, ProblemDetail.forStatusAndDetail(status, ex.getMessage()), ex.getCause());
    return handleExceptionInternal(
        errorResponseException,
        errorResponseException.getBody(),
        errorResponseException.getHeaders(),
        errorResponseException.getStatusCode(),
        request);
  }

  /** Spring Security 防火墙拒绝访问的处理。 */
  @ExceptionHandler(value = {RequestRejectedException.class})
  public ResponseEntity<Object> handleRequestRejectedException(
      RequestRejectedException ex, WebRequest request) {
    log.error("RequestRejectedException Handled  ===> ", ex);
    ErrorResponseException errorResponseException =
        new ErrorResponseException(
            HttpStatus.BAD_REQUEST,
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()),
            ex.getCause());
    return handleExceptionInternal(
        errorResponseException,
        errorResponseException.getBody(),
        errorResponseException.getHeaders(),
        errorResponseException.getStatusCode(),
        request);
  }

  /** 交由 Spring Security 处理 403 权限不足。 */
  @ExceptionHandler(value = {AccessDeniedException.class})
  public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
    throw ex;
  }

  /** 兜底异常处理。 */
  @ExceptionHandler(value = {Throwable.class})
  public ResponseEntity<Object> handleException(Throwable ex, WebRequest request) {
    log.error("System Error Handled  ===> ", ex);
    ErrorResponseException errorResponseException =
        new ErrorResponseException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "System Error"),
            ex.getCause());
    return handleExceptionInternal(
        errorResponseException,
        errorResponseException.getBody(),
        errorResponseException.getHeaders(),
        errorResponseException.getStatusCode(),
        request);
  }
}
