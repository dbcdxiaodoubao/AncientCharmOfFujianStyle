package com.ancientcharmoffujianstyle.config;

import com.ancientcharmoffujianstyle.security.AuthSessionService;
import com.ancientcharmoffujianstyle.security.BearerSessionFilter;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthSessionService sessions, ObjectMapper json)
            throws Exception {
        // Credentials are explicit bearer headers, not browser cookies.
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.formLogin().disable().httpBasic().disable();
        http.exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            json.writeValue(response.getWriter(), ApiResponse.unAuth("请先登录", null));
        }).accessDeniedHandler((request, response, exception) -> {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            json.writeValue(response.getWriter(), new ApiResponse<>(403, "无权访问", null));
        });
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/sysuser/login", "/sysuser/register", "/plan", "/ai/chat").permitAll()
                .antMatchers(HttpMethod.GET, "/map/**", "/recommendation", "/check-in/byfy",
                        "/check-in/tags", "/check-in/comments", "/check-in/dtl/**",
                        "/picture/**", "/demo/**", "/images/**", "/doc.html", "/webjars/**",
                        "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs/**").permitAll()
                .antMatchers("/error").permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(new BearerSessionFilter(sessions, json), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
