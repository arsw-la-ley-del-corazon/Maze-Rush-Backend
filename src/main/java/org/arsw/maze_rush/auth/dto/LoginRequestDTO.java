package org.arsw.maze_rush.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "Datos de login - se puede usar email o username")
public class LoginRequestDTO {
    
    @Schema(
        description = "Nombre de usuario (opcional si se proporciona email)", 
        example = "johndoe",
        maxLength = 50
    )
    private String username;

    @Schema(
        description = "Dirección de correo electrónico (opcional si se proporciona username)", 
        example = "john.doe@example.com",
        format = "email",
        maxLength = 254
    )
    @Email
    private String email;

    @Schema(
        description = "Contraseña del usuario", 
        example = "mySecurePassword123",
        format = "password",
        required = true
    )
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}