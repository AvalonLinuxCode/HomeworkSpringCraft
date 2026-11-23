package com.eduplatform.controller.front;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台课程浏览。
 */
@Tag(name = "前台-课程", description = "已上架课程的浏览与搜索")
@RestController
@RequestMapping("/api/front/courses")
public class CourseFrontController {

    private final CourseService courseService;

    public CourseFrontController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "课程列表/搜索", description = "服务端强制只返回已上架课程")
    @GetMapping
    public Result<PageResult<CourseVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(courseService.search(keyword, pageNum, pageSize));
    }

    @Operation(summary = "课程详情", description = "含 details(JSON 文本)；不存在返回 404")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }
}
