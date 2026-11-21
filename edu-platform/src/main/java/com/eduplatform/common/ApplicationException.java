package com.eduplatform.common;

/**
 * 应用层异常基类。所有业务异常都继承此类，统一由 GlobalExceptionHandler 处理。
 * 子类实现 {@link #statusCode()} 返回对应的 HTTP 状态码。
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    /**
     * 该异常对应的 HTTP 状态码。
     *
     * @return HTTP 状态码，如 400、404、409 等
     */
    public abstract int statusCode();
}
