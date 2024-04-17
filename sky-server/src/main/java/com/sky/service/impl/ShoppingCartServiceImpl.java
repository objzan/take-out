package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        // 根据用户id、菜品id、套餐id、口味查询购物车是否存在数据
        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,cart);
        Long currentUserId = BaseContext.getCurrentId();
        cart.setUserId(currentUserId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cart);


        // 如果存在数据数量+1，不存在则插入数据
        if (shoppingCartList!=null&& shoppingCartList.size()>0){
            ShoppingCart shoppingCart = shoppingCartList.get(0);
            cart.setNumber(shoppingCart.getNumber()+1);
            shoppingCartMapper.update(cart);
        }else {
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId!=null){
                Dish dish = dishMapper.getById(dishId);
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());
            }else if (setmealId!=null){
                Setmeal setmeal = setmealMapper.getById(setmealId);
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());
            }
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }

    }


    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }


    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);

    }


    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        // 查询购物车数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // 判断购物车是否有数据
        if (list!=null&&list.size()>0){
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();
            // 如果当前商品数量等于1删除当前商品
            if (number==1){
                shoppingCartMapper.deleteById(cart.getId());
            }else {
                // 当前商品数量减1
                cart.setNumber(number-1);
                shoppingCartMapper.update(cart);
            }
        }
    }
}
