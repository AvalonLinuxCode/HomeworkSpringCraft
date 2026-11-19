package com.eduplatform.common;

/**
 * 请求的资源不存在，对应 HTTP 404。
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
