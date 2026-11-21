package com.eduplatform.common;

/**
 * 统一 REST 响应体。code 为 HTTP 语义状态码，data 为业务载荷。
 *
 * @param code    状态码，200 表示成功
 * @param message 提示信息
 * @param data    业务数据，失败时为 null
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "ok", data);
    }

    public static Result<Void> success() {
        return new Result<>(200, "ok", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
