package com.eduplatform.vo;

import com.eduplatform.entity.Instructor;

import java.time.OffsetDateTime;

/**
 * 讲师出参。
 */
public record InstructorVO(Long id, String name, String avatar, String title,
                           String description, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

    public static InstructorVO from(Instructor entity) {
        return new InstructorVO(entity.getId(), entity.getName(), entity.getAvatar(), entity.getTitle(),
                entity.getDescription(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
