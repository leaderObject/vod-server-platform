package com.atfangyi.tingshu.common.aspect;

import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.entity.Log;
import com.atfangyi.tingshu.common.sink.OperationLogSink;
import com.atfangyi.tingshu.common.util.AdminAuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class OperationLogAspect {

    private final OperationLogSink operationLogSink;

    @Around("@annotation(operatorLogAnnotation)")
    public Object object(ProceedingJoinPoint proceedingJoinPoint, OperatorLogAnnotation operatorLogAnnotation) {
        try {
            Object proceed = proceedingJoinPoint.proceed();
            return proceed;
        } catch (Throwable e) {
            log.error("出现异常{}", e.getMessage());
            return null;
        } finally {
            //获取操作类型
            String operatorType = operatorLogAnnotation.operatorType();
            MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
            //获取方法名
            String methodName = operatorLogAnnotation.operatorMethod().isEmpty() ? signature.getDeclaringTypeName() + signature.getName() : operatorLogAnnotation.operatorMethod();
            Long userId = AdminAuthContextHolder.getUserId(); //获取操作Id
            Log log1 = new Log();
            log1.setOperateIp(getClientIp());
            log1.setId(userId);
            log1.setOperatorName(null);
            log1.setOperatorType(operatorType);
            log1.setMethodName(methodName);
            log1.setOperateTime(DateTime.now().toString());
            log1.setExtrParams(signature.getDeclaringTypeName()+"."+signature.getName()+"{"+ Arrays.toString(signature.getParameterNames()) +"}");

            try {
                operationLogSink.publish(log1);
            } catch (Exception e) {
                log.warn("oplog publish failed: {}", e.getMessage());
            }
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest req = attrs.getRequest();
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        return ip;
    }

}
