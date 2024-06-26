package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Task {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // select from order where status = ? and order_time<当前时间减15分钟
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);

        if (ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单支付超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }


    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        LocalDateTime time = LocalDateTime.now().plusMinutes(60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
