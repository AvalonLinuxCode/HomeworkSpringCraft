package com.eduplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.mapper.handler.StringArrayTypeHandler;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 课程表 course 的映射对象。tags 为 PostgreSQL {@code text[]}，details 为 {@code jsonb}（以字符串读写）。
 *
 * <p>course 的写入全部走 {@code CourseMapper.xml} 里的自定义语句，BaseMapper 的字段写入策略
 * （{@code FieldStrategy}）在这条路径上根本不生效，故不再标注——免得后人以为改注解能改写入行为。
 * 这里唯一真正被依赖的是 tags 的 typeHandler：{@code selectById} 生成的 autoResultMap 靠它读数组。
 * 时间戳由数据库默认值与触发器维护。
 */
@TableName(value = "course", autoResultMap = true)
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private Long instructorId;

    private Long categoryId;

    private String status;

    private String coverImage;

    @TableField(typeHandler = StringArrayTypeHandler.class)
    private String[] tags;

    private String details;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
