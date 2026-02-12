package com.chenliao.chenliaoblog.config.WebMvcc;

import com.chenliao.chenliaoblog.Interceptor.AdminJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvccConfig implements WebMvcConfigurer {
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminJwtInterceptor())
                // 拦截所有/api开头的请求（根据你的项目路径调整）
                .addPathPatterns("/admin/**")
                // 排除无需验证的路径（登录、注册、公开接口等）
                .excludePathPatterns(
                        "/admin/login"
                );
    }
}
