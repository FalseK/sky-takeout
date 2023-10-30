package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    int countByDishIds(List<Long> ids);

    IPage<SetmealVO> pageQuery(IPage<SetmealVO> page, SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO getByIdWithDishes(Long id);

    List<DishItemVO> getDishesWithImageById(Long id);
}
