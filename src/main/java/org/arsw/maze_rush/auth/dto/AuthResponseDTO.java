package org.arsw.maze_rush.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AuthResponse", description = "Respuesta de autenticación con tokens y datos del usuario")
public class AuthResponseDTO {

    @Schema(
        description = "Token de acceso JWT",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
        description = "Token de renovación",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @Schema(
        description = "Tipo de token",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(
        description = "Tiempo de expiración del token en segundos",
        example = "3600"
    )
    private Long expiresIn;

    @Schema(
        description = "Fecha y hora de expiración del token",
        example = "2024-01-15T16:30:00Z",
        format = "date-time"
    )
    private Instant expiresAt;

    @Schema(description = "Información básica del usuario autenticado")
    private UserInfo user;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "UserInfo", description = "Información básica del usuario")
    public static class UserInfo {

        @Schema(description = "ID único del usuario", example = "user123")
        private String id;

        @Schema(description = "Nombre de usuario", example = "johndoe")
        private String username;

        @Schema(description = "Correo electrónico", example = "john.doe@example.com", format = "email")
        private String email;

        @Schema(description = "Puntuación del usuario", example = "1250")
        @Builder.Default
        private Integer score = 0;

        @Schema(description = "Nivel del usuario", example = "5")
        @Builder.Default
        private Integer level = 1;
    }
}
