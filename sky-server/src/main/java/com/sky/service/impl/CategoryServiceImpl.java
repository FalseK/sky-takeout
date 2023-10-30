package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 分页查询分类
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {

        int currentPage = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();
        Integer type = categoryPageQueryDTO.getType();
        String name = categoryPageQueryDTO.getName();

        Page<Category> page = new Page<>(currentPage, pageSize);

        @SuppressWarnings("unchecked")
        Page<Category> categoryPage = page(page, new LambdaQueryWrapper<Category>()
                .eq(type != null,Category::getType, type)
                .like((name!=null && !"".equals(name)),Category::getName,name)
                .orderBy(true,true,Category::getSort));

        PageResult pageResult = new PageResult(categoryPage.getTotal(), categoryPage.getRecords());

        return Result.success(pageResult);
    }

    @Override
    public Result changeStatus(Integer status, Long id) {

        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();

        boolean flag = updateById(category);

        if (flag){
            return Result.success();
        }

        return Result.error("修改失败");
    }

    @Override
    public Result saveCategory(CategoryDTO categoryDTO) {

        Category category = new Category();

        BeanUtils.copyProperties(categoryDTO,category);

        category.setStatus(0);

        boolean flag = save(category);

        if (flag){
            return Result.success();
        }

        return Result.error("添加失败");


    }

    @Override
    public Result updateCategory(CategoryDTO categoryDTO) {

        Category category = new Category();

        BeanUtils.copyProperties(categoryDTO,category);

        boolean flag = updateById(category);

        if (flag){
            return Result.success();
        }

        return Result.error("修改失败");

    }

    @Override
    public Result<List<Category>> listByType(Integer type) {

        List<Category> list = list(new LambdaQueryWrapper<Category>()
                .eq(type != null,Category::getType, type)
                .eq(Category::getStatus,1));

        return Result.success(list);

    }

    @Override
    public Result deleteById(Long id) {

        int setmealCount = setmealService.count(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));

        int dishCount = dishService.count(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));

        if (setmealCount > 0 || dishCount > 0){
            return Result.error("删除失败");
        }

        removeById(id);

        return Result.success();


    }
}
