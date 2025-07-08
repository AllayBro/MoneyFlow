package com.moneyflow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeAspect.class);
    @Around("execution(public * com.moneyflow..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();

        Object result = null;
        Throwable error = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            long end = System.nanoTime();
            long durationMs = (end - start) / 1_000_000;

            if (error == null) {
                logger.info("Метод {} выполнен за {} мс", joinPoint.getSignature(), durationMs);
            } else {
                logger.warn("Метод {} завершился с ошибкой за {} мс", joinPoint.getSignature(), durationMs);
            }
        }
    }
}
