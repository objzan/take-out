package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询该分类是否有菜品
     * @param CategoryId
     */
    @Select("select count(id) from dish where category_id = #{CategoryId}")
    Integer countByCategoryId(Long CategoryId);


    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据dishId查询dish
     * @param dishId
     * @return
     */
    @Select("select * from dish where id = #{dishId}")
    Dish getById(Long dishId);

    /**
     * 根据菜品id删除菜品
     * @param dishIds
     */
    void deleteBatch(List<Long> dishIds);

    /**
     * 更新菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);


    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("SELECT d.* from dish d LEFT JOIN setmeal_dish sd on d.id = sd.dish_id WHERE sd.setmeal_id=#{setmealId}")
    Dish getBySetmealId(Long setmealId);
}
