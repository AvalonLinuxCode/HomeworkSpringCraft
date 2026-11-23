package com.eduplatform.controller.admin;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.InstructorVO;
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
 * 后台讲师管理。
 */
@Tag(name = "后台-讲师", description = "讲师维护")
@RestController
@RequestMapping("/api/admin/instructors")
public class InstructorAdminController {

    private final InstructorService instructorService;

    public InstructorAdminController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @Operation(summary = "讲师列表", description = "分页 + 关键字搜索；分页参数缺省为 1/10")
    @GetMapping
    public Result<PageResult<InstructorVO>> list(InstructorQuery query) {
        return Result.success(instructorService.list(query));
    }

    @Operation(summary = "讲师详情", description = "不存在返回 404")
    @GetMapping("/{id}")
    public Result<InstructorVO> get(@PathVariable Long id) {
        return Result.success(instructorService.getById(id));
    }

    @Operation(summary = "创建讲师")
    @PostMapping
    public Result<Long> create(@RequestBody InstructorDTO dto) {
        return Result.success(instructorService.create(dto));
    }

    @Operation(summary = "更新讲师", description = "不存在返回 404")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InstructorDTO dto) {
        instructorService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除讲师", description = "名下存在课程时返回 400")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        instructorService.delete(id);
        return Result.success();
    }
}
