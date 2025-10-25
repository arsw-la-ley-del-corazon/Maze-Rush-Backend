package org.arsw.maze_rush.auth.service;

import org.arsw.maze_rush.auth.dto.ForgotPasswordRequestDTO;
import org.arsw.maze_rush.auth.dto.ResetPasswordRequestDTO;

/**
 * Servicio para gestionar recuperación de contraseñas
 */
public interface PasswordResetService {
    
    /**
     * Crea un token de recuperación y lo envía por email
     * @param request Contiene el email del usuario
     */
    void requestPasswordReset(ForgotPasswordRequestDTO request);
    
    /**
     * Resetea la contraseña usando el token
     * @param request Contiene el token y la nueva contraseña
     */
    void resetPassword(ResetPasswordRequestDTO request);
    
    /**
     * Valida si un token es válido
     * @param token Token a validar
     * @return true si es válido, false en caso contrario
     */
    boolean validateToken(String token);
}
