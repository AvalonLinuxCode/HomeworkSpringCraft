package com.eduplatform.common;

/**
 * 请求的资源不存在，对应 HTTP 404。
 */
public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int statusCode() {
        return 404;
    }
}
