package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.utils.CacheClient;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */

    @Transactional
    @Override
    public Result saveSetmeal(SetmealDTO setmealDTO) {



        Setmeal setmeal = new Setmeal();
        BeanUtil.copyProperties(setmealDTO,setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        save(setmeal);

        //清理缓存
        cacheClient.cleanCache(RedisConstant.SETMEAL_CACHE_KEY);

        if (setmealDishes != null && !setmealDishes.isEmpty()){

            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));

            setmealDishMapper.saveBatch(setmealDishes);
        }




        return Result.success();
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int currentPage = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        IPage<SetmealVO> page = new Page<>(currentPage, pageSize);

        IPage<SetmealVO> setmealVOPage = getBaseMapper().pageQuery(page, setmealPageQueryDTO);

        PageResult result = new PageResult();
        result.setRecords(setmealVOPage.getRecords());
        result.setTotal(setmealVOPage.getTotal());

        return Result.success(result);
    }

    /**
     * 菜单启售停售
     * @param status
     * @return
     */
    @Override
    public Result statusChange(Integer status,Long id) {

        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);

        //清理缓存
        cacheClient.cleanCache(RedisConstant.SETMEAL_CACHE_KEY);

        if (status == 0){
            setmeal.setStatus(status);
            updateById(setmeal);
            return Result.success();
        }

        if (setmealDishMapper.countDishesUnSaleById(id) > 0){
            return Result.error(MessageConstant.SETMEAL_ENABLE_FAILED);
        }

        setmeal.setStatus(status);
        updateById(setmeal);

        return Result.success();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public Result deleteBatch(List<Long> ids) {

        if (ids == null || ids.isEmpty()){
            return Result.error("删除失败");
        }

        if (setmealDishMapper.countSetmealOnSaleByIds(ids) > 0){
            return Result.error(MessageConstant.SETMEAL_ON_SALE);
        }

        boolean flag = removeByIds(ids);

        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, ids));

        cacheClient.cleanCache(RedisConstant.SETMEAL_CACHE_KEY);

        if(!flag){
            return Result.error("删除失败");
        }

        return Result.success();
    }

    /**
     * 根据id查询套餐以及对应菜品
     * @param id
     * @return
     */
    @Override
    public Result<SetmealVO> getByIdWithDishes(Long id) {

        if (id == null){
            return Result.error("查询失败");
        }

        SetmealVO setmealVO = getBaseMapper().getByIdWithDishes(id);

        return Result.success(setmealVO);
    }

    @Transactional
    @Override
    public Result updateWithDishes(SetmealDTO setmealDTO) {

        //清理缓存
        cacheClient.cleanCache(RedisConstant.SETMEAL_CACHE_KEY);

        Setmeal setmeal = new Setmeal();

        BeanUtil.copyProperties(setmealDTO,setmeal);

        setmeal.setStatus(0);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId,setmealDTO.getId()));

        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(dish -> dish.setSetmealId(setmealDTO.getId()));
            setmealDishMapper.saveBatch(setmealDishes);
        }

        updateById(setmeal);

        return Result.success();
    }

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @Override
    public Result<List<DishItemVO>> getDishesWithImageById(Long id) {

        List<DishItemVO> dishItemVOList = cacheClient.queryListInCache(
                "setmeal_dishes:",
                id,
                DishItemVO.class,
                baseMapper::getDishesWithImageById,
                null,
                null);

        return Result.success(dishItemVOList);
    }

    /**
     * 根据分类Id查询套餐
     * @param categoryId
     * @return
     */
    @Override
    public Result<List<Setmeal>> listByCategoryId(Long categoryId) {

        List<Setmeal> setmealList = cacheClient.queryListInCache(
                RedisConstant.SETMEAL_CACHE_KEY,
                categoryId, Setmeal.class,
                id -> list(new LambdaQueryWrapper<Setmeal>()
                        .eq(Setmeal::getCategoryId, id)
                        .eq(Setmeal::getStatus, 1)),
                null, null);


        return Result.success(setmealList);
    }
}
