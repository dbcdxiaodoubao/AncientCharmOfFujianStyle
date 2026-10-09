package com.ancientcharmoffujianstyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.query.RegisterQuery;
import com.ancientcharmoffujianstyle.domain.vo.LoginVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询密码
     * @param username
     * @return
     */
    String login(@Param("username") String username);

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    LoginVo getLoginVo(@Param("username") String username);

    /**
     * 查询用户名是否重复
     * @param username
     * @return
     */
    Integer haveOner(@Param("username") String username);

    /**
     * 注册
     * @param registerQuery
     * @return
     */
    void register(RegisterQuery registerQuery);
}
