package com.ancientcharmoffujianstyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.query.LoginQuery;
import com.ancientcharmoffujianstyle.domain.query.RegisterQuery;
import com.ancientcharmoffujianstyle.domain.vo.LoginVo;

public interface ISysUserService extends IService<SysUser> {


    /**
     * 根据用户名验证密码是否正确
     * @param loginQuery
     * @return
     */
    boolean login(LoginQuery loginQuery);

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    LoginVo getLoginVo(String username);

    /**
     * 查询用户名是否重复
     * @param username
     * @return
     */
    Integer haveOner(String username);

    /**
     * 注册
     * @param registerQuery
     * @return
     */
    Void register(RegisterQuery registerQuery);
}
