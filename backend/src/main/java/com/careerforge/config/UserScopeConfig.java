package com.careerforge.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.regex.Pattern;

/** User-scoped endpoints must always match the identity in the verified JWT. */
@Configuration
public class UserScopeConfig implements WebMvcConfigurer {
    private static final Pattern USER_PATH = Pattern.compile("/user/(\\d+)(?:/|$)");
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                var match = USER_PATH.matcher(request.getRequestURI());
                if (!match.find() || "OPTIONS".equals(request.getMethod())) return true;
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !match.group(1).equals(String.valueOf(authentication.getPrincipal()))) {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"You cannot access another user's workspace\"}");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}

