package com.ancientcharmoffujianstyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInTagVo;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn>{

    /**
     * 根据用户id查询列表
     * @param userId
     * @return
     */
    List<CheckInListVo> selectList(Integer userId);

    /**
     * 根据非遗id查询列表
     * @param fyId
     * @return
     */
    List<CheckInListVo> selectListByFy(Integer fyId);

    /**
     * 通过id查询打卡记录详情
     * @param id
     * @return
     */
    CheckInDtlVo dtl(Long id);

    /**
     * 查询有打卡记录的非遗标签（含打卡数量），按打卡数降序
     * @return
     */
    List<CheckInTagVo> listTags();
}
