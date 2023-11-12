package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单管理接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("条件分页查询订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        return orderService.conditionSearch(ordersPageQueryDTO);
    }

    @GetMapping("/statistics")
    @ApiOperation("统计各种状态订单数量")
    private Result<OrderStatisticsVO> statistics(){
        return orderService.statistics();
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        return orderService.details(id);
    }

    @PutMapping("/confirm")
    @ApiOperation("商家接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){

        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderService.updateById(orders);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("商家拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        return orderService.rejection(ordersRejectionDTO);
    }


    @PutMapping("/cancel")
    @ApiOperation("商家拒单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        return orderService.cancel(ordersCancelDTO);
    }


    @PutMapping("/delivery/{id}")
    @ApiOperation("商家拒单")
    public Result delivery(@PathVariable Long id){
        return orderService.delivery(id);
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {

        return orderService.complete(id);
    }

}
