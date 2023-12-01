package com.sky.service;

import com.sky.result.Result;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

public interface WorkspaceService {
    Result<BusinessDataVO> businessData();

    Result<SetmealOverViewVO> overviewSetmeals();

    Result<DishOverViewVO> overviewDishes();

    Result<OrderOverViewVO> overviewOrders();
}
