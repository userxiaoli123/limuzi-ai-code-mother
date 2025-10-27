package com.limuzi.limuziaicodemother.aop;

import com.limuzi.limuziaicodemother.annotation.AuthCheck;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.innerservice.InnerUserService;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodemother.model.enums.UserRoleEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    @Lazy
    private InnerUserService userService;

    /**
     * 拦截方法
     * @param joinPoint 切入点
     * @param authCheck 注解
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = InnerUserService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        if (userRoleEnum== null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
//        要求必须有管理员权限，但是没有
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
//      通过校验
        return joinPoint.proceed();
    }

}