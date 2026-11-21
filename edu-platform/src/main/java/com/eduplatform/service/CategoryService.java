package com.eduplatform.service;

import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.vo.CategoryVO;

import java.util.List;

/**
 * 课程分类服务：树形查询与维护。
 */
public interface CategoryService {

    /** 完整分类树（按 sort_order, id 排序） */
    List<CategoryVO> tree();

    /** 创建分类，返回主键 */
    Long create(CategoryDTO dto);

    /** 更新分类名称/父级/排序，层级随父级自动重算 */
    void update(Long id, CategoryDTO dto);

    /** 删除分类（存在子分类或关联课程时拒绝） */
    void delete(Long id);
}
