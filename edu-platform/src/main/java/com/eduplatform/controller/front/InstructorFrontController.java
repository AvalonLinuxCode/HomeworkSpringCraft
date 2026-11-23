package com.eduplatform.controller.front;

import com.eduplatform.common.Result;
import com.eduplatform.service.CourseService;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.CourseVO;
import com.eduplatform.vo.InstructorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台讲师浏览。
 */
@Tag(name = "前台-讲师", description = "讲师列表与讲师详情")
@RestController
@RequestMapping("/api/front/instructors")
public class InstructorFrontController {

    private static final String STATUS_PUBLISHED = "published";

    private final InstructorService instructorService;
    private final CourseService courseService;

    public InstructorFrontController(InstructorService instructorService, CourseService courseService) {
        this.instructorService = instructorService;
        this.courseService = courseService;
    }

    @Operation(summary = "讲师列表", description = "不分页，最多返回前 200 位讲师")
    @GetMapping
    public Result<List<InstructorVO>> list() {
        return Result.success(instructorService.listAll());
    }

    @Operation(summary = "讲师详情")
    @GetMapping("/{id}")
    public Result<InstructorVO> detail(@PathVariable Long id) {
        return Result.success(instructorService.getById(id));
    }

    @Operation(summary = "讲师所授课程", description = "只返回已上架课程；讲师不存在返回 404")
    @GetMapping("/{id}/courses")
    public Result<List<CourseVO>> courses(@PathVariable Long id) {
        instructorService.getById(id);
        return Result.success(courseService.listByInstructor(id, STATUS_PUBLISHED));
    }
}
