package org.arsw.maze_rush.common.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect aspect;

    @Mock
    private ProceedingJoinPoint pjp;
    @Mock
    private JoinPoint joinPoint; 
    @Mock
    private Signature signature;
    
    @Mock
    private Logger mockLogger;

    private MockedStatic<LoggerFactory> mockedLoggerFactory;
    
    static class DummyService {}

    @BeforeEach
    void setup() {
        mockedLoggerFactory = mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
    }
    
    @AfterEach
    void tearDown() {
        mockedLoggerFactory.close();
    }

    private void setupAopMocks(boolean debugEnabled) {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(DummyService.class); 
        when(signature.getName()).thenReturn("testMethod");
        when(mockLogger.isDebugEnabled()).thenReturn(debugEnabled); 
    }

    //  Around Advice (logMethodExecution)

    @Test
    void testLogMethodExecution_SuccessAndDebugEnabled() throws Throwable {
        setupAopMocks(true); 
        when(pjp.getArgs()).thenReturn(new Object[] { "data" });
        when(pjp.proceed()).thenReturn("OK_RESULT");

        Object result = aspect.logMethodExecution(pjp);

        assertEquals("OK_RESULT", result);
        verify(pjp, times(1)).proceed();
        
        verify(mockLogger, times(1)).debug(eq("→ Entrando a {}.{}() con argumentos: {}"), 
            eq("DummyService"), eq("testMethod"), anyString());
        verify(mockLogger, times(1)).debug(eq("← Saliendo de {}.{}() en {}ms"), 
            eq("DummyService"), eq("testMethod"), anyLong());
        verify(mockLogger, never()).warn(anyString(), any(Object[].class));
    }
    
    @Test
    void testLogMethodExecution_SuccessAndWarningPath() throws Throwable {

        setupAopMocks(false); 
        when(pjp.proceed()).thenReturn("OK");
        
        Object result = aspect.logMethodExecution(pjp);

        assertEquals("OK", result);

        verify(mockLogger, never()).debug(anyString(), any(Object.class), any(Object.class), any(Object.class)); 
    }


    @Test
    void testLogMethodExecution_ExceptionPath() throws Throwable {

        setupAopMocks(true); 
        when(pjp.getArgs()).thenReturn(new Object[] {});
        when(pjp.proceed()).thenThrow(new IllegalArgumentException("Fail Original"));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> aspect.logMethodExecution(pjp));
        
        assertTrue(thrown.getMessage().contains("X Error en"));
        assertEquals("Fail Original", thrown.getCause().getMessage());

        verify(mockLogger, times(1)).debug(anyString(), any(), any(), any());

        verify(mockLogger, never()).error(anyString(), any(Object[].class)); 
        verify(mockLogger, never()).warn(anyString(), any(Object[].class));
    }

    //  AfterThrowing Advice (logException)
 
    @Test
    void testLogException_ShouldLogErrorWithDetails() {

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(DummyService.class); 
        when(signature.getName()).thenReturn("testMethod");
        
        Throwable exception = new NullPointerException("Missing data");
        
        aspect.logException(joinPoint, exception);
        
        verify(mockLogger, times(1)).error(
            "Exception en {}.{}(): {} - {}", 
            "DummyService", 
            "testMethod", 
            "NullPointerException", 
            "Missing data", 
            exception
        );
    }
    
    //  Método de Seguridad (maskSensitiveData)
    
    @Test
    void testMaskSensitiveData_ShouldMaskSensitiveWords() throws Exception {

        Object[] args = {
            "user_normal",
            "mypassword123",
            "tokenABC",
            "client_secret_xyz",
            null
        };

        var method = LoggingAspect.class.getDeclaredMethod("maskSensitiveData", Object[].class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(aspect, new Object[]{ args });

        assertTrue(result.contains("user_normal"));
        assertTrue(result.contains("[PROTECTED]")); 
        assertFalse(result.contains("mypassword123"));
        assertFalse(result.contains("tokenABC"));
        assertTrue(result.contains("null"));
    }
}