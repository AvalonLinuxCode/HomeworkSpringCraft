package com.eduplatform.controller;

import com.eduplatform.common.GlobalExceptionHandler;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.controller.admin.CourseAdminController;
import com.eduplatform.controller.admin.InstructorAdminController;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.service.CourseService;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.CourseVO;
import com.eduplatform.vo.InstructorVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 后台查询接口的 HTTP 语义测试（standalone MockMvc + 真实参数绑定 + 真实
 * {@link GlobalExceptionHandler}，服务层全部 mock，不启动 Spring、不连数据库）。
 *
 * <p>覆盖审计 04 的探针 1 / 2：分页参数缺省必须走默认值而不是绑定报错，
 * 非法状态必须回到 400 + 白名单文案（曾经的 500）。
 */
class AdminQueryBindingTest {

    private CourseService courseService;
    private InstructorService instructorService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        instructorService = mock(InstructorService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CourseAdminController(courseService),
                        new InstructorAdminController(instructorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void courseListWithoutPagingParamsFallsBackToDefaults() throws Exception {
        when(courseService.list(any())).thenReturn(new PageResult<CourseVO>(0, List.of()));

        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));

        ArgumentCaptor<CourseQuery> captor = ArgumentCaptor.forClass(CourseQuery.class);
        verify(courseService).list(captor.capture());
        assertThat(captor.getValue().pageNum()).isEqualTo(1);
        assertThat(captor.getValue().pageSize()).isEqualTo(10);
        assertThat(captor.getValue().keyword()).isNull();
        assertThat(captor.getValue().status()).isNull();
    }

    @Test
    void illegalCourseStatusReturns400WithWhitelistMessage() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                        .param("pageNum", "1")
                        .param("pageSize", "5")
                        .param("status", "xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("课程状态只能是 draft / published / archived"));

        verify(courseService, never()).list(any());
    }

    @Test
    void nonNumericPageNumReturnsReadableChineseMessage() throws Exception {
        mockMvc.perform(get("/api/admin/courses").param("pageNum", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("pageNum 必须是数字"));
    }

    @Test
    void oversizedPageSizeIsCapped() throws Exception {
        when(courseService.list(any())).thenReturn(new PageResult<CourseVO>(0, List.of()));

        mockMvc.perform(get("/api/admin/courses").param("pageSize", "99999"))
                .andExpect(status().isOk());

        ArgumentCaptor<CourseQuery> captor = ArgumentCaptor.forClass(CourseQuery.class);
        verify(courseService).list(captor.capture());
        assertThat(captor.getValue().pageSize()).isEqualTo(CourseQuery.MAX_PAGE_SIZE);
    }

    @Test
    void instructorListWithoutPagingParamsFallsBackToDefaults() throws Exception {
        when(instructorService.list(any())).thenReturn(new PageResult<InstructorVO>(0, List.of()));

        mockMvc.perform(get("/api/admin/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<InstructorQuery> captor = ArgumentCaptor.forClass(InstructorQuery.class);
        verify(instructorService).list(captor.capture());
        assertThat(captor.getValue().pageNum()).isEqualTo(1);
        assertThat(captor.getValue().pageSize()).isEqualTo(10);
    }

    @Test
    void missingCourseSurfacesAsHttp404() throws Exception {
        doThrow(new NotFoundException("课程不存在")).when(courseService).update(eq(999999L), any());

        mockMvc.perform(put("/api/admin/courses/999999")
                        .contentType("application/json")
                        .content("{\"title\":\"x\",\"price\":0,\"instructorId\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("课程不存在"));
    }

    /**
     * 后台编辑表单靠这个端点取全字段：路由曾缺失，前端点「编辑」只会拿到错误、对话框永远打不开。
     * 这里只钉住路由存在（返回体由 service 层用例负责）。
     */
    @Test
    void adminCourseDetailRouteIsMapped() throws Exception {
        when(courseService.getById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/admin/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * N5：内部代码抛的 IllegalArgumentException 不该被伪装成客户端错误，
     * 必须仍然落到底分支返回 500。
     */
    @Test
    void internalIllegalArgumentStillReports500() throws Exception {
        when(courseService.list(any())).thenThrow(new IllegalArgumentException("服务端自己的问题"));

        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("服务器内部错误"));
    }
}
