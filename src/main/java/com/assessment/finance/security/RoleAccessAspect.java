package com.assessment.finance.security;

import com.assessment.finance.exception.ApiException;
import com.assessment.finance.model.Role;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleAccessAspect {

    @Around("@within(com.assessment.finance.security.RoleAccess) || @annotation(com.assessment.finance.security.RoleAccess)")
    public Object enforce(ProceedingJoinPoint joinPoint) throws Throwable {
        RoleAccess roleAccess = resolveRoleAccess(joinPoint);
        if (roleAccess == null) {
            return joinPoint.proceed();
        }

        Role current = RequestContext.getRole();
        for (Role allowed : roleAccess.value()) {
            if (allowed == current) {
                return joinPoint.proceed();
            }
        }

        throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to perform this action");
    }

    private RoleAccess resolveRoleAccess(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RoleAccess onMethod = method.getAnnotation(RoleAccess.class);
        if (onMethod != null) {
            return onMethod;
        }
        return joinPoint.getTarget().getClass().getAnnotation(RoleAccess.class);
    }
}
