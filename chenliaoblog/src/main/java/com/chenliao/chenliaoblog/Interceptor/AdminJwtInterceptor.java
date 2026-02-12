package com.chenliao.chenliaoblog.Interceptor;


import com.chenliao.chenliaoblog.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Interceptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AdminJwtInterceptor implements HandlerInterceptor {
    /**
     * 自定义请求头中令牌的key：直接用token作为参数名
     */
    private static final String TOKEN_HEADER = "token";

    /**
     * 请求处理前执行（核心验证逻辑）
     * @return true-令牌有效，放行；false-令牌无效，拦截
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取token（直接取token参数，无前缀）
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(401);
            log.warn("请求头中未携带token参数");
            return false;
        }

        // 2. 验证令牌有效性（直接验证纯token，无需截取前缀）
        if (!JwtUtil.validateToken(token)) {
            log.warn("JWT令牌验证失败：{}", token);
            response.setStatus(401);
            return false;
        }

        // 3. 令牌有效，解析用户信息并存入request上下文
        Claims claims = JwtUtil.parseToken(token);
        Long userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);

        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        request.setAttribute("jwtClaims", claims);

        log.info("用户 {}({}) 令牌验证通过，放行请求：{}", username, userId, request.getRequestURI());
        return true;
    }

//    /**
//     * 处理令牌无效的情况，返回统一的JSON错误响应
//     */
//    private boolean handleInvalidToken(HttpServletResponse response, String message) throws Exception {
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding("UTF-8");
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("code", 401);
//        result.put("message", message);
//        result.put("success", false);
//
//        try (PrintWriter writer = response.getWriter()) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            writer.write(objectMapper.writeValueAsString(result));
//            writer.flush();
//        }
//
//        return false;
//    }
}
