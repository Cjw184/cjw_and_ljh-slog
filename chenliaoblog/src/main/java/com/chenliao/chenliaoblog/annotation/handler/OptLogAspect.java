package com.chenliao.chenliaoblog.annotation.handler;

import com.alibaba.fastjson.JSON;
import com.chenliao.chenliaoblog.annotation.OperationLog;

import com.chenliao.chenliaoblog.service.OperationLogService;
import com.chenliao.chenliaoblog.utils.IpUtil;
import com.chenliao.chenliaoblog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 操作日志切面类
 * 用于拦截标注了@OperationLog注解的方法，记录操作日志
 */
@Aspect // 声明为切面类
@Component // 交给Spring容器管理，否则切面不生效
public class OptLogAspect {

    // 定义日志对象
    private static final Logger log = LoggerFactory.getLogger(OptLogAspect.class);

    // 注入日志服务（需确保OperationLogService已加@Service注解）
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 切入点：匹配所有标注了@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.chenliao.chenliaoblog.annotation.OperationLog)")
    public void optLog() {
    }

    /**
     * 前置通知：方法执行前触发
     */
    @Before("optLog()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("进入方法[{}]前执行前置通知...", joinPoint.getSignature().getName());
    }

    /**
     * 后置返回通知：方法正常返回后异步记录日志
     * 注意：@Async的异步方法中@Transactional会失效，需根据业务调整
     */
    @Async // 异步执行，不阻塞主线程
    @AfterReturning(value = "optLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取RequestAttributes并校验非空
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                log.warn("获取RequestAttributes失败，跳过日志记录");
                return;
            }

            // 获取HttpServletRequest
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            if (request == null) {
                log.warn("获取HttpServletRequest失败，跳过日志记录");
                return;
            }

            // 通过反射获取切入点方法的注解和信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            // 注解为空则跳过
            if (annotation == null) {
                log.warn("方法[{}]未找到@OperationLog注解，跳过日志记录", method.getName());
                return;
            }

            // 构建操作日志对象
            com.chenliao.chenliaoblog.entity.OperationLog operationLog = new com.chenliao.chenliaoblog.entity.OperationLog();
            // 1. 操作类型（从注解获取）
            operationLog.setOperationType(annotation.operationType().getValue());
            // 2. IP地址
            String ipAddr = IpUtil.getIpAddr(request);
            operationLog.setOperationIp(ipAddr);
            // 3. IP地理位置
            operationLog.setOperaLocation(IpUtil.getIpInfo(ipAddr));
//            // 4. 操作人（处理null情况）
//            String userName = request.getRemoteUser();
//            operationLog.setOperationName(Objects.isNull(userName) ? "未知用户" : userName);
            // 在OptLogAspect的doAfterReturning方法中替换原有的userName逻辑
// 1. 从请求头提取JWT令牌（假设前端把令牌放在Authorization头，格式是Bearer + 空格 + 令牌）
            String token = request.getHeader("token");
            String userName = "未知用户";
            if (token != null ) {
                // 去掉Bearer前缀，得到纯令牌
                try {
                    // 2. 解析JWT令牌（这里用你项目里的JWT工具类，比如JJWT、Hutool-JWT）
                    // 示例：假设你的JWT工具类有parseToken方法，返回包含用户名的Map
                    Map<String, Object> claims = JwtUtil.parseToken(token);
                    // 从claims中取用户名（key根据你生成JWT时的字段名来，比如"username"/"userName"/"userId"）
                    userName = claims.get("username") != null ? claims.get("username").toString() : "未知用户";
                } catch (Exception e) {
                    log.warn("解析JWT令牌获取用户名失败", e);
                }
            }
// 3. 赋值给操作日志
            operationLog.setOperationName(userName);
            // 5. 操作方法名（类名.方法名）
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = className + "." + method.getName();
            operationLog.setMethods(methodName);
            // 6. 方法参数（JSON格式）
            operationLog.setArgs(JSON.toJSONString(joinPoint.getArgs()));
            // 7. 返回结果（JSON格式）
            operationLog.setReturnValue(JSON.toJSONString(result));

            // 保存日志（若需事务，建议在operationLogService的save方法上加@Transactional）
            operationLogService.saveOperationLog(operationLog);
            log.info("操作日志记录成功，方法：{}，操作人：{}", methodName, operationLog.getOperationName());

        } catch (Exception e) {
            // 日志记录失败不影响主业务，仅打印异常
            log.error("记录操作日志失败", e);
        }
    }
}