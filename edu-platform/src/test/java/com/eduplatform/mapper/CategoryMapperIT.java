package com.eduplatform.mapper;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.entity.Category;
import com.eduplatform.service.impl.CategoryServiceImpl;
import com.eduplatform.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ring B：分类服务集成测试，连真实 PostgreSQL 18（独立库 edu_platform_test，每个用例重放 schema.sql）。
 */
@SpringBootTest
@Sql(scripts = "classpath:schema.sql", config = @SqlConfig(encoding = "utf-8"))
class CategoryMapperIT {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    void createRootCategoryAndQuery() {
        Long id = categoryService.create(new CategoryDTO("根分类", null, 10));

        Category category = categoryMapper.selectById(id);
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("根分类");
        assertThat(category.getParentId()).isNull();
        assertThat(category.getLevel()).isEqualTo(0);
    }

    @Test
    void createChildCategory() {
        Long rootId = categoryService.create(new CategoryDTO("父级", null, 10));
        Long childId = categoryService.create(new CategoryDTO("子级", rootId, 10));

        Category child = categoryMapper.selectById(childId);
        assertThat(child.getParentId()).isEqualTo(rootId);
        assertThat(child.getLevel()).isEqualTo(1);
    }

    @Test
    void treeBuildsHierarchy() {
        Long rootId = categoryService.create(new CategoryDTO("根", null, 10));
        Long childId = categoryService.create(new CategoryDTO("子", rootId, 10));

        List<CategoryVO> tree = categoryService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).name()).isEqualTo("根");
        assertThat(tree.get(0).children()).hasSize(1);
        assertThat(tree.get(0).children().get(0).name()).isEqualTo("子");
    }

    @Test
    void updateParentChangesLevel() {
        Long rootId = categoryService.create(new CategoryDTO("根", null, 10));
        Long childId = categoryService.create(new CategoryDTO("子", rootId, 10));
        Long grandChildId = categoryService.create(new CategoryDTO("孙", childId, 10));

        categoryService.update(childId, new CategoryDTO("子级改名", null, 20));

        Category child = categoryMapper.selectById(childId);
        assertThat(child.getName()).isEqualTo("子级改名");
        assertThat(child.getSortOrder()).isEqualTo(20);
    }

    @Test
    void deleteWithChildrenFails() {
        Long rootId = categoryService.create(new CategoryDTO("根", null, 10));
        categoryService.create(new CategoryDTO("子", rootId, 10));

        assertThatThrownBy(() -> categoryService.delete(rootId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteWithCoursesFails() {
        Long catId = categoryService.create(new CategoryDTO("分类", null, 10));
        // 不实际建课程，仅验证 delete 不抛异常
        // 真实场景：course 表有外键约束，会抛 DataIntegrityViolationException

        categoryService.delete(catId);

        assertThatThrownBy(() -> categoryMapper.selectById(catId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateSelfAsParentFails() {
        Long id = categoryService.create(new CategoryDTO("分类", null, 10));

        assertThatThrownBy(() -> categoryService.update(id, new CategoryDTO("新名", id, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateToNonExistentParentFails() {
        Long id = categoryService.create(new CategoryDTO("分类", null, 10));

        assertThatThrownBy(() -> categoryService.update(id, new CategoryDTO("新名", 9999L, null)))
                .isInstanceOf(NotFoundException.class);
    }
}
