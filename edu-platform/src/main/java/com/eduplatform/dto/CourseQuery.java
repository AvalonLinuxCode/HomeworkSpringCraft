package com.eduplatform.dto;

import com.eduplatform.common.ValidationException;

/**
 * 课程列表查询条件。构造时完成归一化与状态白名单校验。
 *
 * <p>分页字段用包装类型：HTTP 绑定时参数缺省传 null，由构造器补默认值。原始类型 {@code int}
 * 会在绑定阶段先报「null 转 int 失败」，下面的归一化逻辑根本轮不到执行。
 */
public record CourseQuery(String keyword, Integer pageNum, Integer pageSize,
                          Long instructorId, Long categoryId, String status) {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public CourseQuery {
        pageNum = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        // 防止 (pageNum-1)*pageSize 溢出
        pageNum = Math.min(pageNum, Integer.MAX_VALUE / pageSize);
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        status = (status == null || status.isBlank()) ? null : status.trim();
        if (status != null && !status.equals("draft") && !status.equals("published") && !status.equals("archived")) {
            throw new ValidationException("课程状态只能是 draft / published / archived");
        }
    }
}
