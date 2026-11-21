package com.eduplatform.dto;

/**
 * 课程新增/修改入参。tags 写入 PostgreSQL text[]，details 为 JSONB 字符串。
 *
 * @implNote 校验集中在 CourseServiceImpl.apply()，与 update() 共用。
 */
public record CourseDTO(String title, String description, java.math.BigDecimal price,
                        Long instructorId, Long categoryId, String coverImage,
                        java.util.List<String> tags, String details) {
}
