package com.eduplatform.service.impl;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.entity.Category;
import com.eduplatform.mapper.CategoryMapper;
import com.eduplatform.service.CategoryService;
import com.eduplatform.vo.CategoryVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_DEPTH = 20;

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectAllOrdered();
        Map<Long, List<Category>> byParent = new LinkedHashMap<>();
        for (Category category : all) {
            Long parentKey = category.getParentId() == null ? 0L : category.getParentId();
            byParent.computeIfAbsent(parentKey, key -> new ArrayList<>()).add(category);
        }
        return buildChildren(0L, byParent);
    }

    private List<CategoryVO> buildChildren(Long parentId, Map<Long, List<Category>> byParent) {
        List<Category> children = byParent.getOrDefault(parentId, List.of());
        List<CategoryVO> result = new ArrayList<>(children.size());
        for (Category child : children) {
            result.add(CategoryVO.from(child, buildChildren(child.getId(), byParent)));
        }
        return result;
    }

    @Override
    @Transactional
    public Long create(CategoryDTO dto) {
        String name = requireName(dto);
        Category category = new Category();
        category.setName(name);
        category.setParentId(dto.parentId());
        category.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        category.setLevel(resolveLevel(dto.parentId()));
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CategoryDTO dto) {
        Category category = requireCategory(id);
        String name = requireName(dto);
        Long newParentId = dto.parentId();
        if (newParentId != null) {
            if (newParentId.equals(id)) {
                throw new ValidationException("不能将分类挂到自身下");
            }
            ensureNoCycle(id, newParentId);
        }
        category.setName(name);
        category.setParentId(newParentId);
        category.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        category.setLevel(resolveLevel(newParentId));
        categoryMapper.updateById(category);
        recomputeDescendantLevels(category.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireCategory(id);
        long children = categoryMapper.countByParentId(id);
        if (children > 0) {
            throw new BusinessException("该分类下存在 " + children + " 个子分类，无法删除");
        }
        long courses = categoryMapper.countCoursesByCategoryId(id);
        if (courses > 0) {
            throw new BusinessException("该分类下存在 " + courses + " 门课程，无法删除");
        }
        try {
            categoryMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("分类存在关联数据，无法删除");
        }
    }

    private Category requireCategory(Long id) {
        Category category = id == null ? null : categoryMapper.selectById(id);
        if (category == null) {
            throw new NotFoundException("分类不存在");
        }
        return category;
    }

    private static String requireName(CategoryDTO dto) {
        if (dto == null) {
            throw new ValidationException("分类信息不能为空");
        }
        String name = dto.name() == null ? null : dto.name().trim();
        if (name == null || name.isEmpty()) {
            throw new ValidationException("分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new ValidationException("分类名称不能超过 50 个字符");
        }
        return name;
    }

    private int resolveLevel(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        Category parent = categoryMapper.selectById(parentId);
        if (parent == null) {
            throw new NotFoundException("父分类不存在");
        }
        return parent.getLevel() + 1;
    }

    /** 沿父链上溯（一条递归 CTE 取整条链），若遇到自身说明会把分类移进自己的子树，形成环。 */
    private void ensureNoCycle(Long id, Long newParentId) {
        if (categoryMapper.isOnParentChain(newParentId, id, MAX_DEPTH)) {
            throw new ValidationException("不能将分类移动到其子分类下");
        }
    }

    /**
     * 父级变化后刷新后代 level。一条递归 CTE 就地算出整棵子树的新层级并批量 UPDATE；
     * 旧实现是先全表加载再逐行 updateById，每次移动分类都要 N+1 次往返。
     */
    private void recomputeDescendantLevels(Long rootId) {
        categoryMapper.refreshDescendantLevels(rootId, MAX_DEPTH);
    }
}
