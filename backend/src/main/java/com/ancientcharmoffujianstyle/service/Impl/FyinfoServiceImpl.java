package com.ancientcharmoffujianstyle.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.service.IFyinfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 古韵闽风 - 非遗信息服务
 * 管理福建省9个地级市的非物质文化遗产数据，
 * 支持按城市检索、按级别排序和名称模糊匹配。
 */
@Service
public class FyinfoServiceImpl extends ServiceImpl<FyinfoMapper, Fyinfo>
        implements IFyinfoService {

    /** 非遗级别权重：世界级(1) > 国家级(2) > 省级(3) > 市级(4) > 县级(5) */
    private static final int[] LEVEL_PRIORITY = {0, 100, 80, 50, 30, 10};

    @Autowired
    FyinfoMapper fyinfoMapper;

    @Override
    public List<FyinfoListVo> listByCity(Long city) {
        List<FyinfoListVo> rawList = fyinfoMapper.listByCity(city);
        // 按非遗级别排序：高级别优先展示
        return rawList.stream()
            .sorted(Comparator.comparingInt(v -> {
                Long level = v.getLevel();
                return level != null && level >= 1 && level <= 5
                    ? -LEVEL_PRIORITY[level.intValue()]
                    : 0;
            }))
            .collect(Collectors.toList());
    }

    /**
     * 按城市和类别筛选非遗项目
     * @param city 城市ID（1-9对应福建9市）
     * @param heritageType 非遗类型（民俗、传统技艺、传统舞蹈等）
     * @return 筛选并排序后的非遗列表
     */
    public List<FyinfoListVo> listByCityAndType(Long city, String heritageType) {
        List<FyinfoListVo> all = listByCity(city);
        if (heritageType == null || heritageType.isEmpty()) {
            return all;
        }
        return all.stream()
            .filter(v -> heritageType.equals(v.getType()))
            .collect(Collectors.toList());
    }
}
