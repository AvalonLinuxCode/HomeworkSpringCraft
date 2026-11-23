package com.eduplatform.controller.admin;

import com.eduplatform.common.Result;
import com.eduplatform.dto.CategoryDTO;
import com.eduplatform.service.CategoryService;
import com.eduplatform.vo.CategoryVO;
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

import java.util.List;

/**
 * 后台分类管理。
 */
@Tag(name = "后台-分类", description = "分类树与分类维护")
@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "分类树", description = "按 sortOrder、id 排序的多级树")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @Operation(summary = "创建分类", description = "parentId 为空即根分类；父分类不存在返回 404")
    @PostMapping
    public Result<Long> create(@RequestBody CategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @Operation(summary = "更新分类", description = "可变更父级；移入自身子树返回 400")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类", description = "存在子分类或关联课程时返回 400")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
