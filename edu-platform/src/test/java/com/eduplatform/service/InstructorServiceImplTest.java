package com.eduplatform.service;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.entity.Instructor;
import com.eduplatform.mapper.InstructorMapper;
import com.eduplatform.service.impl.InstructorServiceImpl;
import com.eduplatform.vo.InstructorVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Ring A：业务规则单元测试。不启动 Spring、不连数据库。
 */
@ExtendWith(MockitoExtension.class)
class InstructorServiceImplTest {

    @Mock
    private InstructorMapper instructorMapper;

    @InjectMocks
    private InstructorServiceImpl instructorService;

    @Test
    void createReturnsGeneratedIdAndTrimsName() {
        when(instructorMapper.insert(any(Instructor.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Instructor.class).setId(7L);
            return 1;
        });

        Long id = instructorService.create(new InstructorDTO("  张老师  ", "/a.png", "资深讲师", "十年经验"));

        assertThat(id).isEqualTo(7L);
        ArgumentCaptor<Instructor> captor = ArgumentCaptor.forClass(Instructor.class);
        verify(instructorMapper).insert(captor.capture());
        Instructor saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("张老师");
        assertThat(saved.getTitle()).isEqualTo("资深讲师");
        assertThat(saved.getAvatar()).isEqualTo("/a.png");
        assertThat(saved.getDescription()).isEqualTo("十年经验");
    }

    @Test
    void createWithBlankNameThrowsValidation() {
        assertThatThrownBy(() -> instructorService.create(new InstructorDTO("   ", null, null, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("姓名");

        verifyNoInteractions(instructorMapper);
    }

    @Test
    void getByIdWithUnknownIdThrowsNotFound() {
        when(instructorMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> instructorService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateWithUnknownIdThrowsNotFoundAndSkipsWrite() {
        when(instructorMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> instructorService.update(99L, new InstructorDTO("张老师", null, null, null)))
                .isInstanceOf(NotFoundException.class);

        verify(instructorMapper, never()).updateById(any(Instructor.class));
    }

    @Test
    void deleteWithCoursesThrowsBusinessAndSkipsWrite() {
        when(instructorMapper.countCoursesByInstructorId(5L)).thenReturn(2L);

        assertThatThrownBy(() -> instructorService.delete(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("2");

        verify(instructorMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteWithoutCoursesDeletesRow() {
        when(instructorMapper.countCoursesByInstructorId(5L)).thenReturn(0L);

        instructorService.delete(5L);

        verify(instructorMapper).deleteById(5L);
    }

    @Test
    void listReturnsTotalAndMappedRecords() {
        when(instructorMapper.count(null)).thenReturn(2L);
        when(instructorMapper.list(null, 10, 0))
                .thenReturn(List.of(instructor(1L, "张老师"), instructor(2L, "李老师")));

        PageResult<InstructorVO> page = instructorService.list(new InstructorQuery(null, 1, 10));

        assertThat(page.total()).isEqualTo(2L);
        assertThat(page.records()).extracting(InstructorVO::name).containsExactly("张老师", "李老师");
    }

    @Test
    void listUsesNormalizedQueryForOffset() {
        when(instructorMapper.count("Java")).thenReturn(0L);
        when(instructorMapper.list("Java", 100, 200)).thenReturn(List.of());

        instructorService.list(new InstructorQuery("  Java  ", 3, 500));

        // 页码<1 归 1、页大小封顶 100、关键字去空白
        verify(instructorMapper).list("Java", 100, 200);
    }

    private static Instructor instructor(Long id, String name) {
        Instructor entity = new Instructor();
        entity.setId(id);
        entity.setName(name);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}
