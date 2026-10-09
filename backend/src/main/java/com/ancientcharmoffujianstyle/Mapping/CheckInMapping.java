package com.ancientcharmoffujianstyle.Mapping;

import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.query.CheckInQuery;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 古韵闽风 - 打卡记录对象映射器
 * 负责打卡数据在请求参数、实体和展示VO之间的双向转换。
 * 支持打卡上传时的Query→Entity映射和查询结果→VO映射。
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckInMapping {

    CheckInMapping INSTANCE = Mappers.getMapper(CheckInMapping.class);

    /** 打卡上传请求 → 实体：忽略自动生成字段 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CheckIn toCreate(CheckInQuery checkInQuery);
}
