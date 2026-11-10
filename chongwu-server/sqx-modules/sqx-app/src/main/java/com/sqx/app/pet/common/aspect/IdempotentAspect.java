package com.sqx.app.pet.common.aspect;

import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.ApiIdempotentService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.shiro.utils.UserContextHolder;
import com.sqx.common.web.utils.HttpContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class IdempotentAspect {

    @Resource
    private ApiIdempotentService apiIdempotentService;

    @Around("@annotation(com.sqx.app.pet.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
        String key = request.getHeader(idempotent.header());
        if (StringUtils.isBlank(key)) {
            return joinPoint.proceed();
        }

        String apiPath = request.getRequestURI();
        Long userId = UserContextHolder.getAppUserId();

        if (apiIdempotentService.isDuplicate(key, apiPath, userId)) {
            throw new BizException(PetError.DUPLICATE_REQUEST);
        }

        Object result = joinPoint.proceed();
        apiIdempotentService.record(key, apiPath, userId, String.valueOf(result), idempotent.expireSeconds());
        return result;
    }
}
