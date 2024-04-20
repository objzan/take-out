package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询购物车
     * @param cart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart cart);


    /**
     * 修改数量
     * @param cart
     */
    @Update("update shopping_cart set number=#{number} where user_id=#{userId}")
    void update(ShoppingCart cart);


    /**
     * 添加购物车
     * @param cart
     */
    void insert(ShoppingCart cart);


    /**
     * 清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);


    /**
     * 根据购物车id删除
     * @param id
     */
    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);

    /**
     * 批量插入菜品到购物车
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
