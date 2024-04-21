package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import kotlin.reflect.KVariance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 营业额统计接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> sumList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double sum = reportMapper.sumByMap(map);
            sum = sum == null ? 0.00 : sum;
            sumList.add(sum);
        }


        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(sumList, ","))
                .build();


        return turnoverReportVO;
    }

    /**
     * 用户统计接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        List<Integer> totalList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            // select count(id) from user where create_time < end
            Integer totalUser = reportMapper.countUserByMap(map);
            // select count(id) from user where create_time >begin and create_time <end
            map.put("begin", beginTime);
            Integer newUser = reportMapper.countUserByMap(map);

            totalList.add(totalUser);
            newUserList.add(newUser);
        }

        UserReportVO userReportVO = UserReportVO
                .builder().dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
        return userReportVO;
    }

    /**
     * 订单统计接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            // 订单列表
            // selete count(id) from order where order_time > begin and order_time<end;
            Integer orderCount = reportMapper.countOrderByMap(map);

            map.put("status", Orders.COMPLETED);
            // selete count(id) from order where order_time>begin and order_time<end and status=5
            // 有效订单数列表
            Integer validOrderCount = reportMapper.countOrderByMap(map);
            orderCountList.add(orderCount);
            validOrderList.add(validOrderCount);

        }


        // 计算总订单数和有效订单数
        Integer orderCountSum = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCountSum = validOrderList.stream().reduce(Integer::sum).get();
        // 计算订单完成率
        Double orderCompletionRate = 0.00;
        if (orderCountSum != 0) {
            orderCompletionRate = validOrderCountSum.doubleValue() / orderCountSum;
        }


        // 封装vo返回
        OrderReportVO orderReportVO = OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(orderCountSum)
                .validOrderCount(validOrderCountSum)
                .build();
        return orderReportVO;
    }

    /**
     * 查询销量排名top10接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO SalesTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = reportMapper.getSalesTop10Report(beginTime,endTime);

        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
        return salesTop10ReportVO;
    }

    /**
     * 查询今日运营数据
     * @param begin
     * @param end
     */
    @Override
    public BusinessDataVO businessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        // 新增用户数
        Integer newUsers = reportMapper.countUserByMap(map);



        // 总订单数
        Integer orderCount = reportMapper.countOrderByMap(map);

        map.put("status",Orders.COMPLETED);
        // 有效订单数
        Integer validOrderCount = reportMapper.countOrderByMap(map);

        // 订单完成率
        Double orderCompletionRate=0.00;
        if (orderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/orderCount;
        }


        // 营业额
        Double turnover = reportMapper.sumByMap(map);

        // 平均客单价
        Double unitPrice=0.00;
        if (validOrderCount!=0){
            unitPrice = turnover.doubleValue()/validOrderCount;
        }


        BusinessDataVO businessDataVO = BusinessDataVO
                .builder()
                .newUsers(newUsers)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();

        return businessDataVO;

    }

    /**
     * 查询套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Integer discontinued = setmealMapper.discontinued();
        Integer sold = setmealMapper.sold();
        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO
                .builder()
                .discontinued(discontinued)
                .sold(sold)
                .build();
        return setmealOverViewVO;
    }


    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        Integer discontinued = dishMapper.discontinued();
        Integer sold = dishMapper.sold();

        DishOverViewVO dishOverViewVO = DishOverViewVO
                .builder()
                .discontinued(discontinued)
                .sold(sold)
                .build();
        return dishOverViewVO;
    }


    /**
     * 查询订单管理数据
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        Integer allOrders = reportMapper.countOrderByMap(map);
        map.put("status",Orders.CANCELLED);
        Integer cancelledOrders = reportMapper.countOrderByMap(map);
        map.put("status",Orders.COMPLETED);
        Integer completedOrders = reportMapper.countOrderByMap(map);
        map.put("status",Orders.DELIVERY_IN_PROGRESS);
        Integer deliveredOrders = reportMapper.countOrderByMap(map);
        map.put("status",Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = reportMapper.countOrderByMap(map);
        OrderOverViewVO orderOverViewVO = OrderOverViewVO
                .builder()
                .allOrders(allOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
        return orderOverViewVO;
    }
}
