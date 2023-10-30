package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    Result saveDish(DishDTO dishDTO);

    Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    Result deleteBatch(List<Long> ids);

    Result<DishVO> getByIdWithFlavors(Long id);

    Result updateDishWithFlavor(DishDTO dishDTO);

    Result<List<Dish>> getByCategoryId(Long categoryId);

    Result changeStatus(Integer status, Long id);

    Result<List<DishVO>> getDishByCategoryIdWithFlavors(Long categoryId);
}
