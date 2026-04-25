package com.campusrunner.interceptor;

import com.campusrunner.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String authHeader = request.getHeader(header);
        String requestUri = request.getRequestURI();
        
        // 如果没有Token，检查是否是公开接口
        if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
            // 公开接口允许匿名访问
            if (isPublicPath(requestUri)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"请先登录\",\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }

        String token = authHeader.substring(tokenPrefix.length() + 1);
        
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));
            request.setAttribute("userType", claims.get("userType"));
            return true;
        } catch (Exception e) {
            // Token验证失败，检查是否是公开接口
            if (isPublicPath(requestUri)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"" + e.getMessage() + "\",\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }
    }
    
    private boolean isPublicPath(String uri) {
        return uri.startsWith("/api/user/register") ||
               uri.startsWith("/api/user/login") ||
               uri.startsWith("/api/admin/login") ||
               uri.startsWith("/api/category/list") ||
               uri.startsWith("/api/task/list");
    }
}
