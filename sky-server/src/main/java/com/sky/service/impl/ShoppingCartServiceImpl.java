package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DishService dishService;

    /**
     * 添加购物车条目
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public Result add(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();

        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart, true);

        Long userID = BaseContext.getCurrentId();

        shoppingCart.setUserId(userID);

        //判断加入菜品是否已存在

        ShoppingCart cart = getShoppingCart(shoppingCartDTO);

        //已存在菜品数量增加
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            updateById(cart);
            return Result.success();
        }

        //不存在新增菜品购物车条目
        Long setmealId = shoppingCartDTO.getSetmealId();
        Long dishId = shoppingCartDTO.getDishId();

        if (setmealId != null) {
            Setmeal setmeal = setmealService.getById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
        } else {
            Dish dish = dishService.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setDishFlavor(shoppingCart.getDishFlavor());
        }

        save(shoppingCart);
        return Result.success();
    }


    /**
     * 查询购物车列表
     * @return
     */
    @Override
    public Result<List<ShoppingCart>> getShoppingCartByUserId() {

        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> list = list(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));

        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @Override
    public Result cleanShoppingCart() {

        Long userId = BaseContext.getCurrentId();

        remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId,userId));

        return Result.success();
    }

    /**
     * 减少购物车一个商品
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public Result sub(ShoppingCartDTO shoppingCartDTO) {

        //判断加入菜品是否已存在
        ShoppingCart cart = getShoppingCart(shoppingCartDTO);

        if (cart == null){
            return Result.error("删除失败");
        }

        if (cart.getNumber() > 1){
            cart.setNumber(cart.getNumber() - 1);

            updateById(cart);

            return Result.success();
        }

        removeById(cart.getId());

        return Result.success();
    }


    /**
     * 获取购物车中内容相同的商品
     * @param shoppingCartDTO
     * @return
     */
    private ShoppingCart getShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();

        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart, true);

        Long userID = BaseContext.getCurrentId();

        shoppingCart.setUserId(userID);

        ShoppingCart cart = getOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getDishId() != null && shoppingCart.getDishFlavor() != null,
                        ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId()));
        return cart;
    }


}
