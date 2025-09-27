package org.arsw.maze_rush.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // segundos
    private Instant expiresAt;
    
    // Datos básicos del usuario autenticado (sin duplicar todo UserResponseDTO)
    private UserInfo user;
    
    @Data
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
    }
}