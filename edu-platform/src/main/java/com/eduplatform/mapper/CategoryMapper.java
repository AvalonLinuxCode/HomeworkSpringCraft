package com.eduplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问。树形查询与关联计数见 CategoryMapper.xml。
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectAllOrdered();

    long countByParentId(@Param("parentId") Long parentId);

    long countCoursesByCategoryId(@Param("categoryId") Long categoryId);
}
