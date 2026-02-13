package com.chenliao.chenliaoblog.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 日志描述
     */
    String desc() default "";

    /**
     * 日志操作类型
     */
    OperationType operationType() default OperationType.SYSTEM;

}
