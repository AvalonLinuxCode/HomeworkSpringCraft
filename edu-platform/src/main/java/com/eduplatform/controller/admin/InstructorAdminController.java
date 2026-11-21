package com.eduplatform.controller.admin;

import com.eduplatform.common.PageResult;
import com.eduplatform.common.Result;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.InstructorVO;
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
@RestController
@RequestMapping("/api/admin/instructors")
public class InstructorAdminController {

    private final InstructorService instructorService;

    public InstructorAdminController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public Result<PageResult<InstructorVO>> list(InstructorQuery query) {
        return Result.success(instructorService.list(query));
    }

    @GetMapping("{id}")
    public Result<InstructorVO> get(@PathVariable Long id) {
        return Result.success(instructorService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody InstructorDTO dto) {
        return Result.success(instructorService.create(dto));
    }

    @PutMapping("{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InstructorDTO dto) {
        instructorService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("{id}")
    public Result<Void> delete(@PathVariable Long id) {
        instructorService.delete(id);
        return Result.success();
    }
}
