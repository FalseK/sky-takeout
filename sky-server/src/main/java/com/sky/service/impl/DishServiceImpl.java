package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

//    @Autowired
//    private SetmealMapper setmealMapper;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @Transactional
    @Override
    public Result saveDish(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        save(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));


            dishFlavorMapper.saveBatch(flavors);

//            dishFlavorService.getBaseMapper().saveBatch(flavors);

        }

        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        int currentPage = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();

//        String name = dishPageQueryDTO.getName();
//        Integer status = dishPageQueryDTO.getStatus();
//        Integer categoryId = dishPageQueryDTO.getCategoryId();

        Page<DishVO> page = new Page<>(currentPage, pageSize);
//
//        Page<Dish> dishPage = page(page, new LambdaQueryWrapper<Dish>().like(name != null, Dish::getName, name)
//                .eq(status != null, Dish::getStatus, status)
//                .eq(categoryId != null, Dish::getCategoryId, categoryId));

        IPage<DishVO> dishVOPage = getBaseMapper().pageQueryDishVo(page, dishPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(dishVOPage.getTotal());
        pageResult.setRecords(dishVOPage.getRecords());

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public Result deleteBatch(List<Long> ids) {

        if (ids == null || ids.isEmpty()){
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

//        if (setmealMapper.countByDishIds(ids) > 0){
//            return Result.error("有关联的套餐，无法删除");
//        }

        if (getBaseMapper().checkDishDelete(ids) > 0){
            return Result.error(MessageConstant.DISH_ON_SALE_AND_BE_RELATED_BY_SETMEAL);
        }


        dishFlavorMapper.deleteBatchByDishIds(ids);



        removeByIds(ids);

        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public Result<DishVO> getByIdWithFlavors(Long id) {
        DishVO dishVO = getBaseMapper().getDishVOById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @Transactional
    @Override
    public Result updateDishWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtil.copyProperties(dishDTO,dish);

        updateById(dish);

        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId,dishDTO.getId()));

        List<DishFlavor> flavorsSource = dishDTO.getFlavors();

        //去掉空白
        List<DishFlavor> flavors = flavorsSource.stream()
                .filter(dishFlavor -> (!"[]".equals(dishFlavor.getValue()) && !"".equals(dishFlavor.getName())))
                .collect(Collectors.toList());


        if (!flavors.isEmpty()){
            //设置dishId
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.saveBatch(flavors);
        }

        return Result.success();
    }


    @Override
    public Result<List<Dish>> getByCategoryId(Long categoryId) {

        List<Dish> list = list(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));

        return Result.success(list);
    }
}
