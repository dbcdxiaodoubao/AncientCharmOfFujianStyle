package com.ancientcharmoffujianstyle.security;

import com.ancientcharmoffujianstyle.utils.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class BearerSessionFilter extends OncePerRequestFilter {
    private final AuthSessionService sessions;
    private final ObjectMapper json;

    public BearerSessionFilter(AuthSessionService sessions, ObjectMapper json) {
        this.sessions = sessions;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            Long userId = header.startsWith("Bearer ") ? sessions.authenticate(header.substring(7)) : null;
            if (userId == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                json.writeValue(response.getWriter(), ApiResponse.unAuth("登录已失效，请重新登录", null));
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
        }
        chain.doFilter(request, response);
    }
}
