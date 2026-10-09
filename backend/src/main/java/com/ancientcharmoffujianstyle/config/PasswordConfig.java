package com.ancientcharmoffujianstyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 古韵闽风 - 安全配置
 * 配置用户密码加密器，用于非遗文旅平台用户登录注册场景。
 * 采用BCrypt自适应哈希算法，强度因子12。
 */
@Configuration
public class PasswordConfig {

    /** BCrypt哈希强度因子，12轮迭代在安全性和登录响应速度之间取得平衡 */
    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    /**
     * 判断明文密码与加密密码是否匹配
     * 供SysUserServiceImpl登录校验调用
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
        return encoder.matches(rawPassword, encodedPassword);
    }
}
