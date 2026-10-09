package com.ancientcharmoffujianstyle.Mapping;

import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 古韵闽风 - 非遗项目对象映射器
 * 负责Fyinfo实体与列表VO、详情VO之间的转换。
 * 支持单个实体映射和批量列表映射。
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FyinfoMapping {

    FyinfoMapping INSTANCE = Mappers.getMapper(FyinfoMapping.class);

    /** 实体 → 详情VO：包含非遗的完整描述文本 */
    FyinfoDtlVo toDtlVo(Fyinfo fyinfo);

    /** 实体 → 列表VO：精简字段适用于地图标注和搜索结果 */
    FyinfoListVo toListVo(Fyinfo fyinfo);

    /** 批量转换：用于城市筛选后的非遗列表返回 */
    List<FyinfoListVo> toListVoList(List<Fyinfo> fyinfoList);
}
