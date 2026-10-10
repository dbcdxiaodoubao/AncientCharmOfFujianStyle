package com.ancientcharmoffujianstyle.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.query.LoginQuery;
import com.ancientcharmoffujianstyle.domain.query.RegisterQuery;
import com.ancientcharmoffujianstyle.domain.vo.LoginVo;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 古韵闽风 - 用户认证服务
 * 负责非遗文旅平台用户的注册、登录、密码安全校验及账户管理。
 * 用户可注册账号后实地打卡福建各地非遗项目。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    /** 用户名规则：4-20位字母数字或中文 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\u4e00-\\u9fa5]{4,20}$");
    /** 密码最短长度 */
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean login(LoginQuery loginQuery) {
        // Every login must resolve to an active persisted user.
        SysUser user = sysUserMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, loginQuery.getUserName()));
        if (user == null || !Long.valueOf(0).equals(user.getStatus())) return false;
        String encodedPassword = sysUserMapper.login(loginQuery.getUserName());
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(loginQuery.getPassword(), encodedPassword);
    }

    @Override
    public Void register(RegisterQuery registerQuery) {
        // 注册前校验用户名和密码格式
        String username = registerQuery.getUserName();
        String rawPassword = registerQuery.getPassword();

        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名需为4-20位字母、数字或中文");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }

        registerQuery.setPassword(passwordEncoder.encode(rawPassword));
        sysUserMapper.register(registerQuery);
        return null;
    }

    @Override
    public LoginVo getLoginVo(String username) {
        LoginVo vo = sysUserMapper.getLoginVo(username);
        if (vo != null) {
            vo.setLoginStatus("SUCCESS");
        }
        return vo;
    }

    @Override
    public Integer haveOner(String username) {
        return sysUserMapper.haveOner(username);
    }

}
