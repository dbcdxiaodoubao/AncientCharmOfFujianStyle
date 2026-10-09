package com.ancientcharmoffujianstyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInTagVo;

import java.util.List;

public interface ICheckInService extends IService<CheckIn> {


    /**
     * 查询打卡信息列表
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
     * 查询有打卡记录的非遗标签（非遗id + 名称 + 打卡数）
     * @return
     */
    List<CheckInTagVo> listTags();
}
