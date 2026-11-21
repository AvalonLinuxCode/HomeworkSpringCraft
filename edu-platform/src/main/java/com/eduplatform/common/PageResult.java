package com.eduplatform.common;

import java.util.List;

/**
 * 分页结果。records 为当前页数据，total 为满足条件的总条数。
 */
public record PageResult<T>(long total, List<T> records) {
    public PageResult {
        records = List.copyOf(records);
    }
}
