package com.eduplatform.dto;

import com.eduplatform.common.ValidationException;

/**
 * 课程列表查询条件。构造时完成归一化与状态白名单校验。
 */
public record CourseQuery(String keyword, int pageNum, int pageSize,
                          Long instructorId, Long categoryId, String status) {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public CourseQuery {
        pageNum = Math.max(pageNum, 1);
        pageSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        pageNum = Math.min(pageNum, Integer.MAX_VALUE / pageSize);
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        status = (status == null || status.isBlank()) ? null : status.trim();
        if (status != null && !status.equals("draft") && !status.equals("published") && !status.equals("archived")) {
            throw new ValidationException("课程状态只能是 draft / published / archived");
        }
    }
}
