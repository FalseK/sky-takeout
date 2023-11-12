package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    IPage<OrderVO> pageQueryOrderVO(IPage<OrderVO> page, OrdersPageQueryDTO ordersPageQueryDTO);


    OrderVO getOrdersVOById(Long id);


}
