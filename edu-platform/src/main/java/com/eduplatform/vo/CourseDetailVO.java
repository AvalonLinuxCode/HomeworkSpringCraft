package com.eduplatform.vo;

import com.eduplatform.entity.Course;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 课程详情出参，比列表多 details（JSONB 原文）。
 */
public record CourseDetailVO(Long id, String title, String description, BigDecimal price,
                             Long instructorId, String instructorName,
                             Long categoryId, String categoryName,
                             String status, String coverImage, String[] tags,
                             String details, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

    public static CourseDetailVO from(Course entity, String instructorName, String categoryName) {
        String[] tags = entity.getTags() == null ? new String[0] : entity.getTags().clone();
        return new CourseDetailVO(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getPrice(),
                entity.getInstructorId(), instructorName, entity.getCategoryId(), categoryName,
                entity.getStatus(), entity.getCoverImage(), tags, entity.getDetails(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
