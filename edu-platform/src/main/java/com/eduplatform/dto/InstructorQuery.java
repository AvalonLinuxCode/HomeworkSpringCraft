package com.eduplatform.dto;

/**
 * 讲师列表查询条件。构造时完成归一化，下游无需再判空/防越界。
 *
 * <p>分页字段用包装类型，理由同 {@link CourseQuery}：参数缺省时绑定阶段传 null，
 * 由构造器补默认值，而不是先撞上「null 转 int 失败」。
 */
public record InstructorQuery(String keyword, Integer pageNum, Integer pageSize) {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public InstructorQuery {
        pageNum = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        // 防止 (pageNum-1)*pageSize 溢出
        pageNum = Math.min(pageNum, Integer.MAX_VALUE / pageSize);
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}
