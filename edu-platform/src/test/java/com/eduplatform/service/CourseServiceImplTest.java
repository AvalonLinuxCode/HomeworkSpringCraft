package com.eduplatform.service.impl;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.entity.Course;
import com.eduplatform.mapper.CategoryMapper;
import com.eduplatform.mapper.CourseMapper;
import com.eduplatform.mapper.InstructorMapper;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
 * Ring A：课程业务规则单元测试。不启动 Spring、不连数据库。
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private InstructorMapper instructorMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void createPersistsAndReturnsId() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());
        when(categoryMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Category());
        when(courseMapper.insertCourse(any(Course.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Course.class).setId(9L);
            return 1;
        });

        Long id = courseService.create(new CourseDTO("标题", "描述", BigDecimal.valueOf(99.99),
                1L, 2L, "cover.png", List.of("tag1"), "{}"));

        assertThat(id).isEqualTo(9L);
        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).insertCourse(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("标题");
        assertThat(captor.getValue().getTags()).containsExactly("tag1");
    }

    @Test
    void createWithNullInstructorThrowsNotFound() {
        when(instructorMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> courseService.create(new CourseDTO("标题", null, BigDecimal.valueOf(99.99),
                1L, null, null, null, null)))
                .isInstanceOf(NotFoundException.class);
        verify(courseMapper, never()).insertCourse(any());
    }

    @Test
    void createWithNegativePriceThrowsValidation() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());

        assertThatThrownBy(() -> courseService.create(new CourseDTO("标题", null, BigDecimal.valueOf(-1),
                1L, null, null, null, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdMissingThrowsNotFound() {
        when(courseMapper.selectDetailById(99L)).thenReturn(null);

        assertThatThrownBy(() -> courseService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateMissingThrowsNotFound() {
        when(courseMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> courseService.update(99L,
                new CourseDTO("新标题", null, BigDecimal.valueOf(10), 1L, null, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTrimsTitle() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("旧标题");
        course.setStatus("draft");
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());

        courseService.update(1L, new CourseDTO("  新标题  ", null, BigDecimal.valueOf(10), 1L, null, null, null, null));

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).updateCourse(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("新标题");
    }

    @Test
    void deleteWithCoursesThrowsBusiness() {
        when(courseMapper.selectById(1L)).thenReturn(new Course());
        when(courseMapper.deleteById(1L)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("fk"));

        assertThatThrownBy(() -> courseService.delete(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void publishIsRejectedWhenGuardMatchesNoRow() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus("published");
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.publishById(1L)).thenReturn(0);

        assertThatThrownBy(() -> courseService.publish(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程已上架，无需重复上架");
    }

    @Test
    void publishWritesWhenGuardAccepts() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus("draft");
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.publishById(1L)).thenReturn(1);

        courseService.publish(1L);

        verify(courseMapper).publishById(1L);
    }

    @Test
    void unpublishIsRejectedWhenGuardMatchesNoRow() {
        Course course = new Course();
        course.setId(1L);
        course.setStatus("draft");
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.unpublishById(1L)).thenReturn(0);

        assertThatThrownBy(() -> courseService.unpublish(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程未上架，无法下架");
    }

    @Test
    void listReturnsPage() {
        when(courseMapper.count("kw", null, null, "published")).thenReturn(2L);
        when(courseMapper.list(eq("kw"), eq(null), eq(null), eq("published"), eq(10), eq(0)))
                .thenReturn(List.of(
                        CourseVO.from(new Course(), "讲师A", "分类A"),
                        CourseVO.from(new Course(), "讲师B", "分类B")));

        PageResult<CourseVO> result = courseService.list(new CourseQuery("kw", 1, 10, null, null, "published"));

        assertThat(result.total()).isEqualTo(2L);
        assertThat(result.records()).hasSize(2);
    }

    @Test
    void createWithMalformedDetailsJsonThrowsValidation() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());

        assertThatThrownBy(() -> courseService.create(new CourseDTO("标题", null, BigDecimal.ONE,
                1L, null, null, null, "{bad json")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("课程详情必须是合法的 JSON");

        verify(courseMapper, never()).insertCourse(any());
    }

    /** readTree 默认允许值后面拖尾巴，PG 却会拒收，所以显式开了 FAIL_ON_TRAILING_TOKENS。 */
    @Test
    void createWithTrailingGarbageAfterDetailsJsonThrowsValidation() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());

        assertThatThrownBy(() -> courseService.create(new CourseDTO("标题", null, BigDecimal.ONE,
                1L, null, null, null, "{\"a\":1} 尾巴")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("课程详情必须是合法的 JSON");
    }

    @Test
    void createWithScalarDetailsJsonThrowsValidation() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());

        assertThatThrownBy(() -> courseService.create(new CourseDTO("标题", null, BigDecimal.ONE,
                1L, null, null, null, "123")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("课程详情必须是合法的 JSON 对象或数组");
    }

    @Test
    void createWithValidDetailsJsonPersistsTextVerbatim() {
        when(instructorMapper.selectById(anyLong())).thenReturn(new com.eduplatform.entity.Instructor());
        when(courseMapper.insertCourse(any(Course.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Course.class).setId(88L);
            return 1;
        });

        courseService.create(new CourseDTO("标题", null, BigDecimal.ONE,
                1L, null, null, null, "{\"chapters\":[]}"));

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).insertCourse(captor.capture());
        assertThat(captor.getValue().getDetails()).isEqualTo("{\"chapters\":[]}");
    }
}
