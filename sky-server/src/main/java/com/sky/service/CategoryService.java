package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    Result changeStatus(Integer status, Long id);

    Result saveCategory(CategoryDTO categoryDTO);

    Result updateCategory(CategoryDTO categoryDTO);

    Result deleteById(Long id);

    Result<List<Category>> listByType(Integer type);
}
