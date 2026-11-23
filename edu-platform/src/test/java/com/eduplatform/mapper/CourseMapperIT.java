package com.eduplatform.mapper;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.PageResult;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.entity.Course;
import com.eduplatform.entity.Instructor;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ring B：课程服务集成测试，连真实 PostgreSQL 18（独立库 edu_platform_test，每个用例重放 schema.sql）。
 */
class CourseMapperIT extends AbstractMapperIT {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private InstructorMapper instructorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CourseService courseService;

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
    void insertAndSelectDetailRoundTrips() throws Exception {
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
        // jsonb 输出会规范化空格（{"chapters": []}），用 JSON 语义比较而非字符串比较
        assertThat(new ObjectMapper().readTree(detail.details()))
                .isEqualTo(new ObjectMapper().readTree("{\"chapters\":[]}"));
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
        c1.setTitle("Zig 语言入门");
        c1.setInstructorId(seedInstructorId);
        c1.setPrice(BigDecimal.ZERO);
        c1.setStatus("published");
        courseMapper.insertCourse(c1);

        Course c2 = new Course();
        c2.setTitle("Zig 并发进阶");
        c2.setInstructorId(seedInstructorId);
        c2.setPrice(BigDecimal.ZERO);
        c2.setStatus("published");
        courseMapper.insertCourse(c2);

        // 注意：simple 分词器按空白切分（中文整段为一个 token），关键字需取标题中的独立词
        PageResult<CourseVO> result = courseService.list(new CourseQuery("Zig", 1, 10, null, null, "published"));
        assertThat(result.total()).isEqualTo(2L);
        assertThat(result.records()).hasSize(2);
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
        insertOrderFor(course.getId());

        assertThatThrownBy(() -> courseService.delete(course.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteWithoutOrderSucceeds() {
        Course course = new Course();
        course.setTitle("无订单的课程");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        courseMapper.insertCourse(course);

        courseService.delete(course.getId());

        assertThat(courseMapper.selectById(course.getId())).isNull();
    }

    @Test
    void countByInstructorIdCountsOnlyThatInstructor() {
        Instructor other = new Instructor();
        other.setName("另一位讲师");
        instructorMapper.insert(other);

        Course course = new Course();
        course.setTitle("计数课程");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        courseMapper.insertCourse(course);

        assertThat(courseMapper.countByInstructorId(seedInstructorId)).isEqualTo(1L);
        assertThat(courseMapper.countByInstructorId(other.getId())).isZero();
        assertThat(courseMapper.countByInstructorId(999L)).isZero();
    }

    private void insertOrderFor(long courseId) {
        long userId;
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.createStatement()
                     .executeQuery("SELECT id FROM users ORDER BY id LIMIT 1")) {
            assertThat(rs.next()).isTrue();
            userId = rs.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException("读取种子用户失败", e);
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO orders (order_no, user_id, course_id, amount) VALUES (?, ?, ?, 0)")) {
            ps.setString(1, "IT-" + UUID.randomUUID());
            ps.setLong(2, userId);
            ps.setLong(3, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("插入订单失败", e);
        }
    }

    /**
     * 上下架守卫子句只能靠真库证明：Ring A 把 mapper 整个 mock 掉了，
     * 删掉 XML 里的 `AND status &lt;&gt; 'published'` 后单元测试与前端全都不会红。
     */
    @Test
    void publishGuardRejectsSecondAttemptInDatabase() {
        Course course = new Course();
        course.setTitle("重复上架守卫");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        course.setStatus("draft");
        courseMapper.insertCourse(course);

        courseService.publish(course.getId());
        assertThat(courseMapper.selectById(course.getId()).getStatus()).isEqualTo("published");

        assertThatThrownBy(() -> courseService.publish(course.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程已上架，无需重复上架");
        assertThat(courseMapper.selectById(course.getId()).getStatus()).isEqualTo("published");
    }

    /** 未上架的守卫是「必须是 published」：归档课程不能被 unpublish 悄悄改成 draft。 */
    @Test
    void unpublishGuardLeavesNonPublishedCourseUntouched() {
        Course course = new Course();
        course.setTitle("归档课程下架守卫");
        course.setInstructorId(seedInstructorId);
        course.setPrice(BigDecimal.ZERO);
        course.setStatus("archived");
        courseMapper.insertCourse(course);

        assertThatThrownBy(() -> courseService.unpublish(course.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程未上架，无法下架");
        assertThat(courseMapper.selectById(course.getId()).getStatus()).isEqualTo("archived");
    }
}
