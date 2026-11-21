package com.eduplatform.controller.front;

import com.eduplatform.common.Result;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.InstructorVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台讲师浏览。
 */
@RestController
@RequestMapping("/api/front/instructors")
public class InstructorFrontController {

    private final InstructorService instructorService;

    public InstructorFrontController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public Result<List<InstructorVO>> list() {
        // 前台讲师列表不分页，直接返回全部
        return Result.success(instructorService.listAll());
    }

    @GetMapping("{id}")
    public Result<InstructorVO> detail(@PathVariable Long id) {
        return Result.success(instructorService.getById(id));
    }
}
