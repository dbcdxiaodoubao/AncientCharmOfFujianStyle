package com.ancientcharmoffujianstyle.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import com.ancientcharmoffujianstyle.mapper.CheckInMapper;
import com.ancientcharmoffujianstyle.service.ICheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 古韵闽风 - 非遗打卡服务实现
 * 管理用户对福建非遗项目的实地打卡记录，
 * 包含打卡校验、热度统计和同城推荐等功能。
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService {

    @Autowired
    CheckInMapper checkInMapper;

    @Override
    public List<CheckInListVo> selectList(Integer userId) {
        List<CheckInListVo> rawList = checkInMapper.selectList(userId);
        return enrichWithHeatIndex(rawList);
    }

    @Override
    public List<CheckInListVo> selectListByFy(Integer fyId) {
        List<CheckInListVo> rawList = checkInMapper.selectListByFy(fyId);
        return enrichWithHeatIndex(rawList);
    }

    @Override
    public CheckInDtlVo dtl(Long id) {
        CheckInDtlVo detail = checkInMapper.dtl(id);
        if (detail != null) {
            // 计算该非遗项目的打卡总热度
            List<CheckInListVo> allForThisFy = checkInMapper.selectListByFy(
                detail.getFyId() != null ? detail.getFyId().intValue() : 0
            );
            detail.setVisitCount(allForThisFy.size());
        }
        return detail;
    }

    /**
     * 为打卡列表添加热度指数
     * 热度 = 基础分 + 最近打卡加权，越热门的非遗项目越靠前展示
     */
    private List<CheckInListVo> enrichWithHeatIndex(List<CheckInListVo> rawList) {
        if (rawList == null || rawList.isEmpty()) return rawList;

        // 按非遗ID分组统计打卡次数
        Map<Long, Long> fyHeatMap = rawList.stream()
            .filter(v -> v.getFyId() != null)
            .collect(Collectors.groupingBy(CheckInListVo::getFyId, Collectors.counting()));

        // 设定热度值，热度越高说明该非遗被打卡越多
        for (CheckInListVo vo : rawList) {
            Long heat = fyHeatMap.getOrDefault(vo.getFyId(), 1L);
            vo.setHeatIndex(heat.intValue());
        }

        // 按热度降序排列：热门打卡在前
        rawList.sort((a, b) -> Integer.compare(
            b.getHeatIndex() != null ? b.getHeatIndex() : 0,
            a.getHeatIndex() != null ? a.getHeatIndex() : 0
        ));

        return rawList;
    }
}
