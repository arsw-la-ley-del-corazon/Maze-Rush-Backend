package org.arsw.maze_rush.common.service;

/**
 * Interfaz para el servicio de envío de emails
 */
public interface EmailService {
    
    /**
     * Envía un email de recuperación de contraseña
     * @param to Email del destinatario
     * @param token Token de recuperación
     */
    void sendPasswordResetEmail(String to, String token);
}
