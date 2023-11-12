package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Orders> {
    Result<OrderSubmitVO> submitOrder(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    Result<PageResult> pageQueryHistoryOrders4User(OrdersPageQueryDTO ordersPageQueryDTO);

    Result<OrderVO> details(Long id);

    Result cancel(Long id);

    Result repetition(Long id);

    Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    Result<OrderStatisticsVO> statistics();

    Result rejection(OrdersRejectionDTO ordersRejectionDTO);

    Result cancel(OrdersCancelDTO ordersCancelDTO);

    Result delivery(Long id);

    Result complete(Long id);
}
