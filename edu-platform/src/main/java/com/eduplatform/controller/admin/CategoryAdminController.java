package com.eduplatform.controller.admin;

import com.eduplatform.common.Result;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.service.CategoryService;
import com.eduplatform.vo.CategoryVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台分类管理。
 */
@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @PostMapping
    public Result<Long> create(@RequestBody CategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @PutMapping("{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
