package com.sqx.app.pet.common.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等注解，基于 api_idempotent 表
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等键请求头名称，默认 Idempotent-Key
     */
    String header() default "Idempotent-Key";

    /**
     * 过期秒数
     */
    int expireSeconds() default 86400;
}
