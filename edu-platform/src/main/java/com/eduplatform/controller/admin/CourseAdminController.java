package com.eduplatform.controller.admin;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台课程管理。
 */
@RestController
@RequestMapping("/api/admin/courses")
public class CourseAdminController {

    private final CourseService courseService;

    public CourseAdminController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Result<PageResult<CourseVO>> list(CourseQuery query) {
        return Result.success(courseService.list(query));
    }

    @PostMapping
    public Result<Long> create(@RequestBody CourseDTO dto) {
        return Result.success(courseService.create(dto));
    }

    @PutMapping("{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        courseService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    @PostMapping("{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publish(id);
        return Result.success();
    }

    @PostMapping("{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublish(id);
        return Result.success();
    }
}
