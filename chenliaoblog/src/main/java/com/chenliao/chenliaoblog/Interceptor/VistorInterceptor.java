package com.chenliao.chenliaoblog.Interceptor;

import com.chenliao.chenliaoblog.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VistorInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // 直接声明为类成员变量，赋固定值（不再用@Value注入）
    // 封禁时间：20分钟 = 1200秒
    private long banTimeSeconds = 1200;
    // 异常访问阈值：1分钟内请求超过100次
    private int threshold = 100;
    // 统计时间窗口：1分钟 = 60秒
    private long windowSeconds = 60;

    // 构造器注入StringRedisTemplate


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取客户端真实IP
        String ip = IpUtil.getIpAddr(request);
        if (ip.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("禁止访问：无法获取IP");
            return false;
        }

        // 2. 检查IP是否在黑名单（Key：blacklist:{ip}）
        String blacklistKey = "chenliaoblog::blacklist:" + ip;
        if (stringRedisTemplate.hasKey(blacklistKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=utf-8");
            log.info("IP已被封禁，请20分钟后重试");
            response.getWriter().write("{\"code\":403,\"msg\":\"IP已被封禁，请20分钟后重试\"}");
            return false;
        }

        // 3. 统计请求次数（Key：ip:request:count:{ip}）
        String countKey = "chenliaoblog:ip:request:count:" + ip;
        Long requestCount = stringRedisTemplate.opsForValue().increment(countKey, 1);
        if (requestCount == 1) {
            // 第一次请求，设置时间窗口过期
            stringRedisTemplate.expire(countKey, windowSeconds, TimeUnit.SECONDS);
            log.info("IP请求次数统计：{}", countKey);
        }

        // 4. 判断是否异常访问
        if (requestCount != null && requestCount > threshold) {
            // 加入黑名单，设置20分钟过期
            stringRedisTemplate.opsForValue().set(
                    blacklistKey,
                    "异常访问：1分钟内请求超过" + threshold + "次",
                    banTimeSeconds,
                    TimeUnit.SECONDS
            );
            log.info("IP因异常访问被封禁：{}", blacklistKey);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"IP因异常访问被封禁20分钟\"}");
            return false;
        }
        log.info("IP正常访问：{}", ip);
        // 5. 正常访问，放行
        return true;
    }
}
