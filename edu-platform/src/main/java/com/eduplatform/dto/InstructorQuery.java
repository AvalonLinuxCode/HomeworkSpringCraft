package com.eduplatform.dto;

/**
 * 讲师列表查询条件。构造时完成归一化，下游无需再判空/防越界。
 */
public record InstructorQuery(String keyword, int pageNum, int pageSize) {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public InstructorQuery {
        pageNum = Math.max(pageNum, 1);
        pageSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}
