package com.rafaellima.hojeafestaenossa.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {

    @Value("${app.admin.key}")
    private String adminKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestKey = request.getHeader("X-Admin-Key");

        if (requestKey == null || !requestKey.equals(adminKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"401\",\"message\":\"Acesso não autorizado\"}");
            return false;
        }

        return true;
    }
}