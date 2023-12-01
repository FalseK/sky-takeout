package com.sky.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SimpleQuery;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<TurnoverReportVO> turnoversStatistics(LocalDate begin, LocalDate end) {


        Map<String, Double> amountMap = SimpleQuery.group(new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getOrderTime, LocalDate.parse("2023-11-10").atTime(LocalTime.MIN))
                        .le(Orders::getOrderTime, LocalDate.parse("2023-11-30").atTime(LocalTime.MAX))
                        .eq(Orders::getStatus, Orders.COMPLETED),
                orders -> orders.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                Collectors.summingDouble(value -> Double.parseDouble(value.getAmount().toString())));

        //获取日期列表
        List<LocalDate> localDateList = getDateList(begin, end);

        String dateList = StrUtil.join(",", localDateList);

        //获取每日订单金额列表
        List<Double> amountList = new ArrayList<>();

        //查询时间范围内无订单
        if (amountMap == null) {
            localDateList.forEach(date -> amountList.add(0d));
        } else {
            localDateList.forEach(date -> {
//                Map<String,Object> amount = amountMap.get(date.toString());
//                amountList.add(amount == null ? 0.0 : Double.parseDouble(amount.get("sumAmount").toString()) );
                Double amount = amountMap.get(date.toString());
                amountList.add(amount != null ? amount : 0.0);

            });
        }

        String turnoverList = StrUtil.join(",", amountList);

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverList)
                .build();

        return Result.success(turnoverReportVO);
    }


    @Override
    public Result<UserReportVO> userStatistics(LocalDate begin, LocalDate end) {

        //统计每日新增用户数
        Map<String, Long> newUserCount = SimpleQuery.group(new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, begin.atTime(LocalTime.MIN))
                        .le(User::getCreateTime, end.atTime(LocalTime.MAX)),
                user -> user.getCreateTime().toLocalDate().toString(),
                Collectors.counting());

        //统计每日总用户数
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        Map<String, Map<String, Object>> sumUserMap = userMapper.countSumUserGroupByCreateTime(map);

        //获取日期列表
        List<LocalDate> localDateList = getDateList(begin, end);

        //构造日期字符串
        String dateList = StrUtil.join(",", localDateList);

        //构造每日新增用户数字符串
        List<Long> newUserList;
        if (newUserCount != null) {
            newUserList = localDateList.stream()
                    .map(localDate -> {
                        Long dayNewUsers = newUserCount.get(localDate.toString());
                        return (dayNewUsers != null) ? dayNewUsers : 0;
                    })
                    .collect(Collectors.toList());


        } else {
            newUserList = localDateList.stream().map(localDate -> 0L).collect(Collectors.toList());
        }


        String dayNewUserStr = StrUtil.join(",", newUserList);

        //构造每日总用户数字符串

        List<Long> daySumUserList = new ArrayList<>();
        if (sumUserMap != null) {

            Long tempSumUser = 0L;
            for (LocalDate localDate : localDateList) {
                Map<String, Object> userCountMap = sumUserMap.get(localDate.toString());
                if (userCountMap != null) {
                    tempSumUser = (Long) userCountMap.get("sumUser");
                }
                daySumUserList.add(tempSumUser);
            }

        } else {
            daySumUserList = localDateList.stream().map(localDate -> 0L).collect(Collectors.toList());
        }


        String daySumUserStr = StrUtil.join(",", daySumUserList);

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(dateList)
                .newUserList(dayNewUserStr)
                .totalUserList(daySumUserStr)
                .build();

        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Result<OrderReportVO> orderStatistics(LocalDate begin, LocalDate end) {

        //获取日期列表
        List<LocalDate> dateList = getDateList(begin, end);

        //构造日期字符串
        String dateStr = StrUtil.join(",", dateList);

        LambdaQueryWrapper<Orders> timeWrapper = new LambdaQueryWrapper<Orders>()
                .between(Orders::getOrderTime, begin.atTime(LocalTime.MIN), end.atTime(LocalTime.MAX));

//        //查询订单总数
//        int totalOrders = (int) orderService.count(timeWrapper);
//
//        //查询有效订单数
//        int validOrders = (int) orderService.count(timeWrapper.eq(Orders::getStatus, Orders.COMPLETED));

        //查询每日订单数
        Map<String, Long> totalOrderCountMap =
                SimpleQuery.group(timeWrapper,
                        orders -> orders.getOrderTime().toLocalDate().toString(),
                        Collectors.counting());

        //计算每日订单总数
        long totalOrders = totalOrderCountMap.values().stream().mapToLong(value -> value).sum();

        //构造订单数字符串
        String orderCountStr = getStrByMap(dateList, totalOrderCountMap);


        //查询有效订单数
        Map<String, Long> validOrderCountMap = SimpleQuery.group(
                timeWrapper.eq(Orders::getStatus, Orders.COMPLETED),
                orders -> orders.getOrderTime().toLocalDate().toString(),
                Collectors.counting());

        //计算有效订单总数
        long validOrders = validOrderCountMap.values().stream().mapToLong(value -> value).sum();

        //构造有效订单数字符串
        String validOrderCountStr = getStrByMap(dateList, validOrderCountMap);

        //计算订单完成率
        double orderCompletionRate = ((double) validOrders / (double) (totalOrders == 0 ? 1 : totalOrders));

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(dateStr)
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(orderCountStr)
                .validOrderCountList(validOrderCountStr)
                .validOrderCount((int) validOrders)
                .totalOrderCount((int) totalOrders)
                .build();


        return Result.success(orderReportVO);
    }

    @Override
    public Result<SalesTop10ReportVO> top10(LocalDate begin, LocalDate end) {

        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",Orders.COMPLETED);

        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.top10Goods(map);
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String nameStr = StrUtil.join(",", nameList);
        String numberStr = StrUtil.join(",", numberList);
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(nameStr)
                .numberList(numberStr)
                .build();

        return Result.success(salesTop10ReportVO);
    }

    /**
     * 从map中构造字符串
     * @param dateList
     * @param countMap
     * @return
     */
    private String getStrByMap(List<LocalDate> dateList, Map<String, Long> countMap) {
        List<Long> totalOrderCountList = dateList.stream().map(date -> {
            Long totalOrdersCount = countMap.get(date.toString());
            return totalOrdersCount != null ? totalOrdersCount : 0;
        }).collect(Collectors.toList());

        return StrUtil.join(",", totalOrderCountList);
    }

    /**
     * 获取日期列表
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> localDateList = new ArrayList<>();
        localDateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            localDateList.add(begin);
        }
        return localDateList;
    }
}
