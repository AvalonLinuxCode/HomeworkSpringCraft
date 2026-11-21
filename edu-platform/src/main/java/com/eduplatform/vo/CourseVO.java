package com.eduplatform.vo;

import com.eduplatform.entity.Course;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 课程列表出参（不含 details 大字段）。
 *
 * @implNote 字段显式列出，新增实体字段需手动加入 from()，避免反射泄漏。
 */
public record CourseVO(Long id, String title, String description, BigDecimal price,
                       Long instructorId, String instructorName,
                       Long categoryId, String categoryName,
                       String status, String coverImage, String[] tags,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {

    public static CourseVO from(Course entity, String instructorName, String categoryName) {
        String[] tags = entity.getTags() == null ? new String[0] : entity.getTags().clone();
        return new CourseVO(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getPrice(),
                entity.getInstructorId(), instructorName, entity.getCategoryId(), categoryName,
                entity.getStatus(), entity.getCoverImage(), tags,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
