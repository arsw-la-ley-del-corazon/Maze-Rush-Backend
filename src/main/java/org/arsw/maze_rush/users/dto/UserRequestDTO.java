package org.arsw.maze_rush.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UserRequest", description = "Datos de entrada para crear o actualizar un usuario")
public class UserRequestDTO {
    
    @Schema(
        description = "Nombre de usuario único", 
        example = "johndoe",
        minLength = 3,
        maxLength = 50
    )
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Schema(
        description = "Dirección de correo electrónico válida", 
        example = "john.doe@example.com",
        format = "email",
        maxLength = 254
    )
    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @Schema(
        description = "Contraseña del usuario (mínimo 8 caracteres)", 
        example = "mySecurePassword123",
        minLength = 8,
        maxLength = 72,
        format = "password"
    )
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
