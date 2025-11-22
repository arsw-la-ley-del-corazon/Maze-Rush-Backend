package org.arsw.maze_rush.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UpdateProfileRequest", description = "Datos para actualizar el perfil del usuario")
public class UpdateProfileRequestDTO {
    
    @Schema(
        description = "Nuevo nombre de usuario (opcional)", 
        example = "newusername",
        minLength = 3,
        maxLength = 50
    )
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @Schema(
        description = "Nuevo email (opcional)", 
        example = "newemail@example.com",
        format = "email",
        maxLength = 254
    )
    @Email(message = "Formato de email inválido")
    @Size(max = 254, message = "El email no puede tener más de 254 caracteres")
    private String email;

    @Schema(
        description = "Biografía del usuario (opcional)", 
        example = "Cazador experto de laberintos",
        maxLength = 200
    )
    @Size(max = 200, message = "La bio no puede tener más de 200 caracteres")
    private String bio;

    @Schema(
        description = "Color del avatar en formato hexadecimal (opcional)", 
        example = "#A46AFF",
        pattern = "^#[0-9A-Fa-f]{6}$"
    )
    @Size(min = 7, max = 7, message = "El color debe estar en formato hex (#RRGGBB)")
    private String avatarColor;

    @Schema(
        description = "Tamaño preferido de laberinto (opcional)", 
        example = "Mediano",
        allowableValues = {"Pequeño", "Mediano", "Grande"}
    )
    private String preferredMazeSize;
}
