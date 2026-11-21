package com.eduplatform.dto;

/**
 * 分类新增/修改入参。level 由服务端按 parentId 推导，不接受前端传入。
 */
public record CategoryDTO(String name, Long parentId, Integer sortOrder) {
}
