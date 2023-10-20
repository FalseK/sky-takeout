package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

//    List<DishVO> pageQueryDishVo(IPage<DishVO> page, DishPageQueryDTO dishPageQueryDTO);

    IPage<DishVO> pageQueryDishVo(IPage<DishVO> page, DishPageQueryDTO dishPageQueryDTO);

    DishVO getDishVOById(Long id);

    int checkDishDelete(List<Long> ids);


}
