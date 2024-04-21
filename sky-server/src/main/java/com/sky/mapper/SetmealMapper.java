package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询是否关联套餐
     * @param CategoryId
     * @return
     */
    @Select("select count(id) from setmeal where category_id=#{CategoryId}")
    Integer countByCategoryId(Long CategoryId);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);


    /**
     * 批量删除套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id=#{id}")
    Setmeal getByIdWithSetmealDishes(Long id);

    /**
     * 修改套餐
     * @param setmeal
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据分类id查询套餐
     * @param setmeal
     * @return
     */
    @Select("select * from setmeal where category_id=#{categoryId} and status = #{status}")
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name,d.description,d.image,sd.copies from setmeal_dish sd left join dish d on sd.dish_id = d.id where sd.setmeal_id=#{setmealId}")
    List<DishItemVO> getDishItemById(Long setmealId);

    /**
     * 查询停售套餐数量
     * @return
     */
    @Select("select count(id) from setmeal where status=0")
    Integer discontinued();

    /**
     * 查询起售套餐数量
     * @return
     */
    @Select("select count(id) from setmeal where status=1")
    Integer sold();
}
