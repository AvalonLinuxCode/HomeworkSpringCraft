package com.eduplatform.service.impl;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.entity.Category;
import com.eduplatform.mapper.CategoryMapper;
import com.eduplatform.service.CategoryService;
import com.eduplatform.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ring A：分类业务规则单元测试。不启动 Spring、不连数据库。
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createRootCategory() {
        when(categoryMapper.selectById(anyLong())).thenReturn(null);

        Long id = categoryService.create(new CategoryDTO("根分类", null, 10));

        assertThat(id).isNotNull();
        verify(categoryMapper).insert(any(Category.class));
    }

    @Test
    void createWithNullNameThrowsValidation() {
        assertThatThrownBy(() -> categoryService.create(new CategoryDTO(null, null, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithEmptyNameThrowsValidation() {
        assertThatThrownBy(() -> categoryService.create(new CategoryDTO("   ", null, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithChildParentNotFoundThrowsNotFound() {
        when(categoryMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.create(new CategoryDTO("子分类", 99L, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateSelfAsParentThrowsValidation() {
        Category cat = new Category();
        cat.setId(5L);
        cat.setName("分类");
        cat.setLevel(0);
        when(categoryMapper.selectById(5L)).thenReturn(cat);

        assertThatThrownBy(() -> categoryService.update(5L, new CategoryDTO("新名", 5L, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void deleteWithChildrenThrowsBusiness() {
        when(categoryMapper.selectById(1L)).thenReturn(new Category());
        when(categoryMapper.countByParentId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteWithCoursesThrowsBusiness() {
        Category cat = new Category();
        cat.setId(1L);
        when(categoryMapper.selectById(1L)).thenReturn(cat);
        when(categoryMapper.countByParentId(1L)).thenReturn(0L);
        when(categoryMapper.countCoursesByCategoryId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void treeBuildsHierarchy() {
        Category root = new Category();
        root.setId(1L);
        root.setName("根");
        root.setParentId(null);
        root.setLevel(0);
        root.setSortOrder(10);

        Category child = new Category();
        child.setId(2L);
        child.setName("子");
        child.setParentId(1L);
        child.setLevel(1);
        child.setSortOrder(10);

        when(categoryMapper.selectAllOrdered()).thenReturn(List.of(root, child));

        List<CategoryVO> tree = categoryService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).name()).isEqualTo("根");
        assertThat(tree.get(0).children()).hasSize(1);
        assertThat(tree.get(0).children().get(0).name()).isEqualTo("子");
    }
}
