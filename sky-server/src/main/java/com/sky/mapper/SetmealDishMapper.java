package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据dishId查询菜品套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealDishIdsByDishId(List<Long> dishIds);
}
