package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询该分类是否有菜品
     * @param CategoryId
     */
    @Select("select count(id) from dish where category_id = #{CategoryId}")
    Integer countByCategoryId(Long CategoryId);



}
