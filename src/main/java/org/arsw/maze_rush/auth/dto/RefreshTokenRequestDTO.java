package org.arsw.maze_rush.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RefreshTokenRequest", description = "Solicitud para renovar el token de acceso")
public class RefreshTokenRequestDTO {
    
    @Schema(
        description = "Token de renovación para obtener un nuevo token de acceso", 
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}