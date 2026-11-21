package com.eduplatform.vo;

import com.eduplatform.entity.Category;

import java.util.List;

/**
 * 分类出参，children 非空表示有子分类。
 */
public record CategoryVO(Long id, String name, Long parentId, Integer sortOrder, Integer level,
                         List<CategoryVO> children) {

    public static CategoryVO from(Category entity, List<CategoryVO> children) {
        return new CategoryVO(entity.getId(), entity.getName(), entity.getParentId(),
                entity.getSortOrder(), entity.getLevel(), children);
    }
}
