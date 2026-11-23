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

    /**
     * 从 {@code startId} 沿 parent_id 上溯（含起点本身），判断链上是否出现 {@code ancestorId}。
     * 用于移动分类时的成环检测；{@code maxDepth} 是防御性跳数上限，即使库里已有脏数据构成的
     * 环也不会失控。
     */
    boolean isOnParentChain(@Param("startId") Long startId, @Param("ancestorId") Long ancestorId,
                            @Param("maxDepth") int maxDepth);

    /**
     * 递归重算 {@code rootId} 整棵子树的 level（root 自身按当前值起算），返回真正被改动的行数。
     */
    int refreshDescendantLevels(@Param("rootId") Long rootId, @Param("maxDepth") int maxDepth);
}
