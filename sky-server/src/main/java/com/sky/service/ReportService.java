package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {
    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计接口
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO SalesTop10Report(LocalDate begin, LocalDate end);


    /**
     * 查询今日运营数据
     * @param begin
     * @param end
     */
    BusinessDataVO businessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO overviewSetmeals();

    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO overviewOrders();
}
