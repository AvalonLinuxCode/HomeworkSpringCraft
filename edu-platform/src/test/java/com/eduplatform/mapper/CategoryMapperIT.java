package com.eduplatform.mapper;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.entity.Category;
import com.eduplatform.entity.Course;
import com.eduplatform.entity.Instructor;
import com.eduplatform.service.impl.CategoryServiceImpl;
import com.eduplatform.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ring B：分类服务集成测试，连真实 PostgreSQL 18（独立库 edu_platform_test，每个用例重放 schema.sql）。
 *
 * <p>注意：schema.sql 重放后自带种子分类（前端开发/后端开发等），涉及全局树大小的断言需按名称定位。
 */
@SpringBootTest
class CategoryMapperIT extends AbstractMapperIT {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private InstructorMapper instructorMapper;

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

        // 种子数据自带 2 个根分类，不能按总数断言，按名称定位本次创建的节点
        CategoryVO root = tree.stream().filter(node -> "根".equals(node.name())).findFirst().orElseThrow();
        assertThat(root.children()).extracting(CategoryVO::name).isEqualTo(List.of("子"));
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
        Long instructorId = instructorMapper.selectList(null).get(0).getId();

        Course course = new Course();
        course.setTitle("关联课程");
        course.setInstructorId(instructorId);
        course.setCategoryId(catId);
        course.setPrice(BigDecimal.ZERO);
        courseMapper.insertCourse(course);

        assertThatThrownBy(() -> categoryService.delete(catId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteEmptyCategoryRemovesRow() {
        Long id = categoryService.create(new CategoryDTO("临时分类", null, 10));

        categoryService.delete(id);

        assertThat(categoryMapper.selectById(id)).isNull();
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
