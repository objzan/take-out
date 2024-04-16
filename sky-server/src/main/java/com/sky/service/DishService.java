package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveWithFavors(DishDTO dishDTO);


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);


    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO getByIdWithFlavors(Long id);


    /**
     * 修改菜品
     * @param dishDTO
     */
    void updateWithFlavors(DishDTO dishDTO);

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

//    /**
//     * 根据分类查询起售菜品
//     * @param dish
//     * @return
//     */
    List<DishVO> listWithFlavor(Dish dish);
}
