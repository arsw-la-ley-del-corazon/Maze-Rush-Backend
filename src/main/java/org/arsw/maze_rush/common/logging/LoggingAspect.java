package org.arsw.maze_rush.common.logging;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspecto AOP para logging automático de métodos en servicios, controladores y repositorios.
 * Registra entrada, salida, duración y excepciones de manera global.
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut para todos los métodos en paquetes de controllers
     */
    @Pointcut("execution(* org.arsw.maze_rush..controller..*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut para todos los métodos en paquetes de services
     */
    @Pointcut("execution(* org.arsw.maze_rush..service..*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut para todos los métodos en paquetes de repositories
     */
    @Pointcut("execution(* org.arsw.maze_rush..repository..*(..))")
    public void repositoryMethods() {}

    /**
     * Around advice que envuelve la ejecución de métodos para logging completo
     */
    @SuppressWarnings("java:S2139") 
    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        // Log de entrada
        if (logger.isDebugEnabled()) {
            Object[] args = joinPoint.getArgs();
            logger.debug("→ Entrando a {}.{}() con argumentos: {}", 
                className, methodName, args.length > 0 ? maskSensitiveData(args) : "[]");
        }

        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log de salida exitosa
            if (logger.isDebugEnabled()) {
                logger.debug("← Saliendo de {}.{}() en {}ms", className, methodName, duration);
            } else if (duration > 1000) {
                logger.warn("⚠ {}.{}() tomó {}ms en ejecutarse", className, methodName, duration);
            }
            
            return result;
            
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("X Error en {}.{}() después de {}ms: {} - {}", 
                className, methodName, duration, e.getClass().getSimpleName(), e.getMessage(), e);


            if (e instanceof RuntimeException re) {
                throw re;
            }

            if (e instanceof Error err) {
                throw err;
            }

            throw new IllegalStateException(
                String.format("Error no controlado en %s.%s() tras %dms", className, methodName, duration),
                e
            );
        }
    }

    /**
     * Advice para capturar y loggear todas las excepciones
     */
    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods() || repositoryMethods()", 
                   throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        logger.error("Exception en {}.{}(): {} - {}", 
            className, methodName, exception.getClass().getSimpleName(), exception.getMessage(), exception);
    }

    /**
     * Enmascara datos sensibles en los logs
     */
    private String maskSensitiveData(Object[] args) {
        return Arrays.toString(Arrays.stream(args)
            .map(arg -> {
                if (arg == null) return "null";
                String str = arg.toString();
                // Ocultar información sensible (passwords, tokens, etc.)
                if (str.toLowerCase().contains("password") || 
                    str.toLowerCase().contains("token") ||
                    str.toLowerCase().contains("secret")) {
                    return "[PROTECTED]";
                }
                return str;
            })
            .toArray());
    }
}
