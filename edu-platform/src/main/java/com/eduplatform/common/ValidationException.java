package com.eduplatform.common;

/**
 * 入参校验失败，对应 HTTP 400。
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
