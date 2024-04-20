package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 判断地址簿是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        // 判断购物车是否为空
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(currentId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(UUID.randomUUID().toString().replaceAll("-", ""));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orderMapper.insert(orders);

        // 向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);


        // 清空当前用户购物车
        shoppingCartMapper.deleteByUserId(currentId);


        // 封装返回vo
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().orderTime(orders.getOrderTime()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).id(orders.getId()).build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        simulatePaymentSuccess(ordersPaymentDTO.getOrderNumber());
        OrderPaymentVO vo = new OrderPaymentVO();
        return vo;
        // 当前登录用户id
        // Long userId = BaseContext.getCurrentId();
        // User user = userMapper.getById(userId);
        // 调用微信支付接口，生成预支付交易单
        // JSONObject jsonObject = weChatPayUtil.pay(
        //         ordersPaymentDTO.getOrderNumber(), // 商户订单号
        //         new BigDecimal(0.01), // 支付金额，单位 元
        //         "苍穹外卖订单", // 商品描述
        //         user.getOpenid() // 微信用户的openid
        // );
        //
        // JSONObject jsonObject = new JSONObject();
        // if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
        //     throw new OrderBusinessException("该订单已支付");
        // }
        //
        // OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        // vo.setPackageStr(jsonObject.getString("package"));

    }


    /**
     * 模拟支付成功，并调用paySuccess方法修改订单状态
     *
     * @param orderNumber 订单号
     */
    private void simulatePaymentSuccess(String orderNumber) {
        // 模拟支付成功，这里仅作为示例，实际中应该根据支付接口返回的结果来判断
        // 调用paySuccess来更新订单状态
        try {
            paySuccess(orderNumber);
        } catch (Exception e) {
            // 处理paySuccess方法执行过程中可能出现的异常
            throw new RuntimeException("支付成功后更新订单状态失败", e);
        }
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder().id(ordersDB.getId()).status(Orders.TO_BE_CONFIRMED).payStatus(Orders.PAID).checkoutTime(LocalDateTime.now()).build();

        orderMapper.update(orders);


        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+outTradeNo);
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }


    /**
     * 查看订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        // 根据id查询订单信息
        Orders orders = orderMapper.getById(id);

        // 根据id查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        // 封装vo返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setStatus(ordersPageQueryDTO.getStatus());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            List<Orders> ordersList = page.getResult();
            for (Orders orders : ordersList) {
                Long ordersId = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(ordersId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }


        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        Orders orderDB = orderMapper.getById(id);

        // 判断订单是否为空
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 判断订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orderDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDB.getId());

        // 待接单状态退款
        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            orders.setPayStatus(Orders.CANCELLED);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        // 获取订单菜品
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单对象转换成购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            // 将菜品数据对象拷贝到购物车对象 忽略id不拷贝
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }


    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
    /**
     * 各个状态订单数量的统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }


    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orderDB = orderMapper.getById(ordersRejectionDTO.getId());
        if (orderDB==null||!orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDB.getId());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {

        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orderDB = orderMapper.getById(id);
        if (orderDB==null||!orderDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);

    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orderDB = orderMapper.getById(id);
        if (orderDB==null||!orderDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders =new Orders();
        orders.setId(orderDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


}
