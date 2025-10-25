package org.arsw.maze_rush.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para autenticación con token OAuth2 de Google
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de autenticación OAuth2 con Google")
public class OAuth2LoginRequestDTO {
    
    @NotBlank(message = "El token de Google es obligatorio")
    @Schema(description = "Token de ID de Google obtenido del frontend", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    private String idToken;
}
