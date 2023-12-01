package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SimpleQuery;
import com.sky.dto.OrderCountDto;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.result.Result;
import com.sky.service.*;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private UserService userService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DishService dishService;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 查询今日运营数据
     *
     * @return
     */
    @Override
    public Result<BusinessDataVO> businessData() {

        LocalDate today = LocalDate.now();

        long newUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getCreateTime, today));

//        long totalOrders = orderService.count(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderTime, today));
//
//        long validOrderCount = orderService.count(
//                new LambdaQueryWrapper<Orders>()
//                        .eq(Orders::getOrderTime, today)
//                        .eq(Orders::getStatus, Orders.COMPLETED));

        OrderCountDto orderCountDto = orderMapper.countTodayOrders(today);

        int totalOrders = orderCountDto.getTotalOrders();
        int validOrderCount = orderCountDto.getValidOrders();
        double turnover = orderCountDto.getTurnover();


        //计算订单完成率
        double orderCompletionRate = ((double) validOrderCount / (double) (totalOrders == 0 ? 1 : totalOrders));

        //计算平均客单价
        double unitPrice = turnover / (double) (validOrderCount == 0 ? 1 : validOrderCount);

        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .newUsers((int) newUsers)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();

        return Result.success(businessDataVO);
    }

    @Override
    public Result<SetmealOverViewVO> overviewSetmeals() {

        long discontinued = setmealService.count(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, 0));

        long sold = setmealService.count(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, 1));

        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO.builder()
                .discontinued((int) discontinued)
                .sold((int) sold)
                .build();

        return Result.success(setmealOverViewVO);
    }


    @Override
    public Result<DishOverViewVO> overviewDishes() {
        int discontinued = (int) dishService.count(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 0));
        int sold = (int) dishService.count(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1));

        DishOverViewVO dishOverViewVO = DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();

        return Result.success(dishOverViewVO);
    }

    @Override
    public Result<OrderOverViewVO> overviewOrders() {
        OrderOverViewVO orderOverViewVO = orderMapper.overViewOrders(LocalDate.now());
        return Result.success(orderOverViewVO);
    }
}
