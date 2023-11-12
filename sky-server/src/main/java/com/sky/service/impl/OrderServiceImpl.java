package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;

import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.properties.BaiduApiProperties;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.service.UserService;
import com.sky.utils.BaiduUtil;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopProperties shopProperties;

    @Autowired
    private BaiduUtil baiduUtil;

    @Transactional
    @Override
    public Result<OrderSubmitVO> submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //判断地址是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();

        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        String address = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();


        try {
            checkOutOfRange(address);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }


        //判断用户购物车是否为空
        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));

        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //插入订单数据
        Orders orders = new Orders();
        BeanUtil.copyProperties(ordersSubmitDTO, orders);

        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(address);
        save(orders);

        //插入orderDetail数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shoppingCart -> {
            OrderDetail orderDetail = BeanUtil.copyProperties(shoppingCart, OrderDetail.class);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetailList);

        //删除用户购物车数据
        shoppingCartService.remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.append("code", "ORDERPAID");

        OrderPaymentVO vo = jsonObject.toBean(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getStr("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        //发现没有将支付时间 check_out属性赋值，所以在这里更新

        LocalDateTime checkOutTime = LocalDateTime.now();

        update(new LambdaUpdateWrapper<Orders>().eq(Orders::getNumber, orderNumber)
                .eq(Orders::getUserId, userId)
                .set(Orders::getStatus, Orders.TO_BE_CONFIRMED)
                .set(Orders::getPayStatus, Orders.PAID)
                .set(Orders::getCheckoutTime, checkOutTime));
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = getOne(new LambdaQueryWrapper<Orders>().eq(Orders::getNumber, outTradeNo));

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        updateById(orders);
    }

    /**
     * 分页查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQueryHistoryOrders4User(OrdersPageQueryDTO ordersPageQueryDTO) {

        int currentPage = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();

        Page<OrderVO> page = new Page<>(currentPage, pageSize);

        IPage<OrderVO> orderDTOPage = baseMapper.pageQueryOrderVO(page, ordersPageQueryDTO);

//        List<OrderVO> ordersDTOList = orderDTOPage.getRecords();
//
//        List<OrderVO> collect = ordersDTOList.stream().map(ordersDTO -> {
//            OrderVO orderVO = new OrderVO();
//            orderVO.setOrderDetailList(ordersDTO.getOrderDetails());
//            return orderVO;
//        }).collect(Collectors.toList());

        PageResult result = new PageResult();
        result.setTotal(orderDTOPage.getTotal());
        result.setRecords(orderDTOPage.getRecords());

        return Result.success(result);
    }


    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public Result<OrderVO> details(Long id) {

        if (id == null) {
            return Result.error("查询失败");
        }

        OrderVO ordersVO = baseMapper.getOrdersVOById(id);
        return Result.success(ordersVO);
    }

    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @Override
    public Result cancel(Long id) {

        Orders orders = getById(id);

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //TODO 调用微信退款接口
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);

        return Result.success();
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @Override
    public Result repetition(Long id) {

        Long userId = BaseContext.getCurrentId();

        Orders orders = getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, id));

        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = BeanUtil.copyProperties(orderDetail, ShoppingCart.class, "id");
            shoppingCart.setUserId(userId);
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(shoppingCartList);

        return Result.success();
    }


    /**
     * 管理端条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {

        int currentPage = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();
        Page<OrderVO> page = new Page<>(currentPage, pageSize);

        IPage<OrderVO> orderVOPage = baseMapper.pageQueryOrderVO(page, ordersPageQueryDTO);

        List<OrderVO> orderVOList = orderVOPage.getRecords();

        orderVOList.forEach(orderVO -> orderVO.setOrderDishes(getOrderDishesStr(orderVO)));

        return Result.success(new PageResult(orderVOPage.getTotal(), orderVOList));
    }

    /**
     * 统计各状态订单数量
     *
     * @return
     */
    @Override
    public Result<OrderStatisticsVO> statistics() {
        int toBeConfirmed = count(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.TO_BE_CONFIRMED));
        int confirmed = count(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.CONFIRMED));
        int deliveryInProgress = count(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS));

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return Result.success(orderStatisticsVO);
    }


    /**
     * 商家拒单
     *
     * @param ordersRejectionDTO
     * @return
     */
    @Override
    public Result rejection(OrdersRejectionDTO ordersRejectionDTO) {
        String rejectionReason = ordersRejectionDTO.getRejectionReason();
        Long id = ordersRejectionDTO.getId();

        Orders ordersDB = getById(id);

        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            return Result.error("订单不存在");
        }

        //订单已付款
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            // TODO 调用微信退款接口

        }

        //订单状态为待付款
        Orders orders = new Orders();
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(rejectionReason);
        orders.setId(ordersDB.getId());
        updateById(orders);


        return Result.success();
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     * @return
     */
    @Override
    public Result cancel(OrdersCancelDTO ordersCancelDTO) {

        Long orderId = ordersCancelDTO.getId();
        String cancelReason = ordersCancelDTO.getCancelReason();

        Orders orderDB = getById(orderId);

        if (orderDB == null){
            return Result.error("订单不存在");
        }

        if (orderDB.getPayStatus().equals(Orders.PAID)){
            // TODO 调用微信退款接口
        }

        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(cancelReason);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);


        return Result.success();
    }

    @Override
    public Result delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        updateById(orders);

        return Result.success();
    }


    @Override
    public Result complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        updateById(orders);
        return Result.success();
    }

    /**
     * 获取订单商品简报
     *
     * @param orderVO
     * @return
     */
    private String getOrderDishesStr(OrderVO orderVO) {
        List<OrderDetail> odList = orderVO.getOrderDetailList();

        List<String> dishesStr = odList.stream()
                .map(od -> od.getName() + "*" + od.getNumber() + ";")
                .collect(Collectors.toList());

        return String.join("", dishesStr);
    }


    /**
     * 判断是否超出配送范围
     *
     * @param address
     */
    private void checkOutOfRange(String address) {

        String userLatLng = baiduUtil.getUserAddressLatLng(address);

        String shopLat = String.valueOf(shopProperties.getLat());
        String shopLng = String.valueOf(shopProperties.getLng());
        String shopLngLat = shopLat + "," + shopLng;

        Integer distance = baiduUtil.getRouteDistance(shopLngLat, userLatLng);

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

}


