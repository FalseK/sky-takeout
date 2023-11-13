package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sky.entity.Orders;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    /**
     * 每分钟检查超时订单并自动取消
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){

        log.info("定时处理超时订单：{}",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        orderService.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getStatus,Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime,time)
                .set(Orders::getStatus,Orders.CANCELLED)
                .set(Orders::getCancelReason,"订单超时,自动取消")
                .set(Orders::getCancelTime,LocalDateTime.now()));

    }

    /**
     * 每天凌晨1点执行一次
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){

        log.info("定时处理未完成订单:{}",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusHours(1);
        orderService.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS)
                .lt(Orders::getOrderTime,time)
                .set(Orders::getStatus,Orders.COMPLETED)
                .set(Orders::getCancelTime,LocalDateTime.now()));

    }

}
