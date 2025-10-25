package org.arsw.maze_rush.auth.service;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;

/**
 * Servicio para manejar autenticación OAuth2
 */
public interface OAuth2Service {
    
    /**
     * Autentica un usuario usando el token de ID de Google
     * @param request Contiene el token de ID de Google
     * @return AuthResponseDTO con los tokens JWT y datos del usuario
     */
    AuthResponseDTO authenticateWithGoogle(OAuth2LoginRequestDTO request);
    
    /**
     * Procesa la autenticación OAuth2 después del callback exitoso
     * @param email Email del usuario autenticado por Google
     * @param name Nombre del usuario
     * @param providerId ID del proveedor (Google sub)
     * @param profileImageUrl No utilizado (para compatibilidad)
     * @return AuthResponseDTO con los tokens JWT y datos del usuario
     */
    AuthResponseDTO processOAuth2User(String email, String name, String providerId, String profileImageUrl);
}
