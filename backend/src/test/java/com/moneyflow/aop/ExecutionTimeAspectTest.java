package com.moneyflow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExecutionTimeAspectTest {

    private ExecutionTimeAspect aspect;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setup() {
        aspect = new ExecutionTimeAspect();
        joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("Mocked.method()");
        when(joinPoint.getSignature()).thenReturn(signature);
    }

    @Test
    void testSuccessfulExecution() throws Throwable {
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logExecutionTime(joinPoint);

        assertEquals("OK", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testExecutionWithException() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test failure"));

        Throwable ex = assertThrows(RuntimeException.class, () -> aspect.logExecutionTime(joinPoint));
        assertEquals("Test failure", ex.getMessage());

        verify(joinPoint, times(1)).proceed();
    }
}
