package com.eduplatform.controller.admin;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "后台-课程", description = "课程维护与上下架")
@RestController
@RequestMapping("/api/admin/courses")
public class CourseAdminController {

    private final CourseService courseService;

    public CourseAdminController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "课程列表",
            description = "分页 + 关键字/讲师/分类/状态筛选；分页参数缺省为 1/10，status 非法返回 400")
    @GetMapping
    public Result<PageResult<CourseVO>> list(CourseQuery query) {
        return Result.success(courseService.list(query));
    }

    @Operation(summary = "课程详情", description = "后台编辑表单取全字段；不存在返回 404")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> get(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @Operation(summary = "创建课程", description = "details 必须是合法 JSON 对象/数组，否则返回 400")
    @PostMapping
    public Result<Long> create(@RequestBody CourseDTO dto) {
        return Result.success(courseService.create(dto));
    }

    @Operation(summary = "更新课程", description = "课程不存在返回 404")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        courseService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除课程", description = "存在订单记录时返回 400")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    @Operation(summary = "上架课程", description = "已是上架态返回 400")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publish(id);
        return Result.success();
    }

    @Operation(summary = "下架课程", description = "本就未上架返回 400")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublish(id);
        return Result.success();
    }
}
