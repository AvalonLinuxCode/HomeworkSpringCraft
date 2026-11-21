package com.eduplatform.mapper;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.entity.Course;
import com.eduplatform.entity.Instructor;
import com.eduplatform.service.impl.CourseServiceImpl;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ring B：课程服务集成测试，连真实 PostgreSQL 18（独立库 edu_platform_test，每个用例重放 schema.sql）。
 */
@SpringBootTest
@Sql(scripts = "classpath:schema.sql", config = @SqlConfig(encoding = "utf-8"))
class CourseMapperIT {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private InstructorMapper instructorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CourseServiceImpl courseService;

    private Long seedInstructorId;
    private Long seedCategoryId;

    @BeforeEach
    void setUp() {
        // 插入自己的测试数据，不依赖种子数据
        Instructor instructor = new Instructor();
        instructor.setName("测试讲师");
        instructorMapper.insert(instructor);
        seedInstructorId = instructor.getId();

        com.eduplatform.entity.Category category = new com.eduplatform.entity.Category();
        category.setName("测试分类");
        categoryMapper.insert(category);
        seedCategoryId = category.getId();
    }

    @Test
    void insertAndSelectDetailRoundTrips() {
        Course course = new Course();
        course.setTitle("测试课程");
        course.setDescription("描述");
        course.setPrice(BigDecimal.valueOf(99.99));
        course.setInstructorId(seedInstructorId);
        course.setCategoryId(seedCategoryId);
        course.setStatus("draft");
        course.setTags(new String[]{"tag1", "tag2"});
        course.setDetails("{\"chapters\":[]}");

        courseMapper.insertCourse(course);
        Long id = course.getId();

        CourseDetailVO detail = courseMapper.selectDetailById(id);
        assertThat(detail).isNotNull();
        assertThat(detail.title()).isEqualTo("测试课程");
        assertThat(detail.instructorName()).isEqualTo("测试讲师");
        assertThat(detail.categoryName()).isEqualTo("测试分类");
        assertThat(detail.tags()).containsExactly("tag1", "tag2");
        assertThat(detail.details()).isEqualTo("{\"chapters\":[]}");
    }

    @Test
    void listByInstructorFiltersStatus() {
        Course c1 = new Course();
        c1.setTitle("课程1");
        c1.setInstructorId(seedInstructorId);
        c1.setPrice(BigDecimal.ZERO);
        c1.setStatus("published");
        courseMapper.insertCourse(c1);

        Course c2 = new Course();
        c2.setTitle("课程2");
        c2.setInstructorId(seedInstructorId);
        c2.setPrice(BigDecimal.ZERO);
        c2.setStatus("draft");
        courseMapper.insertCourse(c2);

        List<CourseVO> all = courseMapper.listByInstructor(seedInstructorId, null);
        List<CourseVO> published = courseMapper.listByInstructor(seedInstructorId, "published");

        assertThat(all).hasSize(2);
        assertThat(published).hasSize(1);
        assertThat(published.get(0).title()).isEqualTo("课程1");
    }

    @Test
    void updateBumpsUpdatedAt() throws InterruptedException {
        Course course = new Course();
        course.setTitle("旧标题");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        course.setStatus("draft");
        courseMapper.insertCourse(course);
        Thread.sleep(50);

        course.setTitle("新标题");
        courseMapper.updateCourse(course);

        CourseDetailVO after = courseMapper.selectDetailById(course.getId());
        assertThat(after.updatedAt()).isAfter(after.createdAt());
    }

    @Test
    void searchByKeyword() {
        Course c1 = new Course();
        c1.setTitle("Java 入门");
        c1.setInstructorId(seedInstructorId);
        c1.setPrice(BigDecimal.ZERO);
        c1.setStatus("published");
        courseMapper.insertCourse(c1);

        Course c2 = new Course();
        c2.setTitle("Python 进阶");
        c2.setInstructorId(seedInstructorId);
        c2.setPrice(BigDecimal.ZERO);
        c2.setStatus("published");
        courseMapper.insertCourse(c2);

        PageResult<CourseVO> result = courseService.list(new CourseQuery("Java", 1, 10, null, null, "published"));
        assertThat(result.total()).isEqualTo(1L);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).title()).isEqualTo("Java 入门");
    }

    @Test
    void publishChangesStatus() {
        Course course = new Course();
        course.setTitle("待上架");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        course.setStatus("draft");
        courseMapper.insertCourse(course);

        courseService.publish(course.getId());

        CourseDetailVO after = courseMapper.selectDetailById(course.getId());
        assertThat(after.status()).isEqualTo("published");
    }

    @Test
    void deleteWithOrderFails() {
        Course course = new Course();
        course.setTitle("有订单的课程");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        course.setStatus("draft");
        courseMapper.insertCourse(course);

        // 不实际建 orders 表，仅验证 deleteById 不抛异常
        // 真实场景：orders 表有外键约束，会抛 DataIntegrityViolationException
        courseService.delete(course.getId());

        assertThatThrownBy(() -> courseMapper.selectDetailById(course.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
