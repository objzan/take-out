package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类 id 查询套餐是否关联分类
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId};")
    Integer countByCategoryId(Long id);
}
