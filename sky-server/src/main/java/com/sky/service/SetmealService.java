package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    Result saveSetmeal(SetmealDTO setmealDTO);

    Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    Result statusChange(Integer status,Long id);

    Result deleteBatch(List<Long> ids);

    Result<SetmealVO> getByIdWithDishes(Long id);

    Result updateWithDishes(SetmealDTO setmealDTO);
}
