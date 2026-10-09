package com.ancientcharmoffujianstyle.Mapping;

import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.vo.SysUserDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.SysUserListVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 古韵闽风 - 用户对象映射器
 * 负责SysUser实体与各层VO之间的字段转换，
 * 避免手写getter/setter赋值，减少样板代码。
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysUserMapping {

    SysUserMapping INSTANCE = Mappers.getMapper(SysUserMapping.class);

    /** 将数据库实体转为前端展示用的详情VO */
    SysUserDtlVo toDtlVo(SysUser sysUser);

    /** 将实体转为列表VO，仅暴露非敏感字段 */
    @Mapping(target = "password", ignore = true)
    SysUserListVo toListVo(SysUser sysUser);
}
