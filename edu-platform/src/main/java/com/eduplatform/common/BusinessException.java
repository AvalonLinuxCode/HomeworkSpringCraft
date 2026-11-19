package com.eduplatform.common;

/**
 * 业务规则拒绝执行（如删除被关联数据引用的讲师），对应 HTTP 400。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
