package com.ancientcharmoffujianstyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FyinfoMapper extends BaseMapper<Fyinfo> {

    /**
     * 通过所属城市查询非遗信息列表
     * @param city
     * @return
     */
    List<FyinfoListVo> listByCity(Long city);

}
