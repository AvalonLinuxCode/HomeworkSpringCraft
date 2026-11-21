package com.eduplatform.controller.front;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.service.CourseService;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import com.eduplatform.vo.InstructorVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台课程浏览。
 */
@RestController
@RequestMapping("/api/front/courses")
public class CourseFrontController {

    private final CourseService courseService;

    public CourseFrontController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Result<PageResult<CourseVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(courseService.search(keyword, pageNum, pageSize));
    }

    @GetMapping("{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }
}
