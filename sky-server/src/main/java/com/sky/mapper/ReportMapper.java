package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /**
     * 计算每天营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 统计用户数量
     * @param map
     * @return
     */
    Integer countUserByMap(Map map);

    /**
     * 统计订单数量
     * @return
     */
    Integer countOrderByMap(Map map);


    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10Report(LocalDateTime begin, LocalDateTime end);


}
