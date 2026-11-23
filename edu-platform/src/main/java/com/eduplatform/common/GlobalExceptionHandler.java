package com.eduplatform.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把应用异常与常见请求错误转换为统一 {@link Result} 响应。
 *
 * <p>约定：客户端可纠正的输入问题一律返回 4xx 且带中文提示，500 只留给服务端故障。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Result<Void>> handleApplication(ApplicationException e) {
        return ResponseEntity.status(e.statusCode()).body(Result.error(e.statusCode(), e.getMessage()));
    }

    /**
     * 记录式查询对象（{@code CourseQuery} / {@code InstructorQuery}）由 Spring 在构造期完成归一化与
     * 校验，构造器抛出的 {@link ApplicationException} 会被包装成 {@link BeanInstantiationException}。
     * 不解包就会掉进 {@link #handleUnexpected} 变成 500，这里还原它本来的 4xx 语义。
     *
     * <p>只认 {@link ApplicationException}：构造器里抛出别的东西（NPE、裸 IllegalArgumentException）
     * 是我们自己的缺陷，必须继续按 500 + error 日志暴露出来，不能伪装成客户端错误——与把
     * {@code IllegalArgumentException} 移出 400 兜底组是同一个理由。查询 DTO 想拒绝非法入参，
     * 请抛 {@link ValidationException}。
     */
    @ExceptionHandler(BeanInstantiationException.class)
    public ResponseEntity<Result<Void>> handleQueryInstantiation(BeanInstantiationException e) {
        ApplicationException wrapped = unwrapApplicationException(e);
        return wrapped != null ? handleApplication(wrapped) : handleUnexpected(e);
    }

    /** 参数绑定与 {@code @Valid} 校验失败（MethodArgumentNotValidException 是它的子类）。 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleInvalidArgument(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::describeFieldError)
                .findFirst()
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(Result.error(400, message));
    }

    /** 类型转换失败时 Spring 给的是英文内部文案，换成前端能直接展示的中文。 */
    private static String describeFieldError(FieldError error) {
        if (error.getCode() != null && error.getCode().startsWith("typeMismatch")) {
            return error.getField() + " 必须是数字";
        }
        return error.getField() + ": " + error.getDefaultMessage();
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(Result.error(400, "请求参数不合法: " + e.getMessage()));
    }

    /**
     * 未映射的路径（例如前端调了个不存在的端点）此前会一路掉进 {@link #handleUnexpected} 变成 500，
     * 把「客户端 URL 写错」伪装成「服务端故障」。Spring 6 的默认资源兜底抛
     * {@link NoResourceFoundException}，这里显式收口成 404。
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Result<Void>> handleNoEndpoint(Exception e) {
        return ResponseEntity.status(404).body(Result.error(404, "接口不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.status(500).body(Result.error(500, "服务器内部错误"));
    }

    private static ApplicationException unwrapApplicationException(Throwable error) {
        for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ApplicationException application) {
                return application;
            }
        }
        return null;
    }
}
