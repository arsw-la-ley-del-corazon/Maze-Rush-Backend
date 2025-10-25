package org.arsw.maze_rush.common.exceptions;

/**
 * Excepción para solicitudes inválidas (HTTP 400)
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
