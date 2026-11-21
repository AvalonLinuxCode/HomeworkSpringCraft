package com.eduplatform.common;

public class BusinessException extends ApplicationException {
    public BusinessException(String message) {
        super(message);
    }
    @Override
    public int statusCode() {
        return 400;
    }
}
