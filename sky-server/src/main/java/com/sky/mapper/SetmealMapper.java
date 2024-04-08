package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询是否关联套餐
     * @param CategoryId
     * @return
     */
    @Select("select count(id) from setmeal where category_id=#{CategoryId}")
    Integer countByCategoryId(Long CategoryId);
}
