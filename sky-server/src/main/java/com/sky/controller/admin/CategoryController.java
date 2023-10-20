package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/list")
    @ApiOperation("查询菜品分类")
    public Result<List<Category>> list(Integer type){
        return categoryService.listByType(type);

    }

    @GetMapping("/page")
    @ApiOperation("条件分页查询分类")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO){

        return categoryService.pageQuery(categoryPageQueryDTO);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用分类")
    public Result changeStatus(@PathVariable Integer status, Long id){
        return categoryService.changeStatus(status,id);
    }

    @PostMapping
    @ApiOperation("新增分类")
    public Result saveCategory(@RequestBody CategoryDTO categoryDTO){
        return categoryService.saveCategory(categoryDTO);
    }

    @PutMapping
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){
        return categoryService.updateCategory(categoryDTO);
    }

    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result deleteById(Long id){

        return categoryService.deleteById(id);

    }

}
