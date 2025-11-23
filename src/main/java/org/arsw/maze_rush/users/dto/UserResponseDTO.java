package org.arsw.maze_rush.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserResponse", description = "Respuesta con información del usuario")
public class UserResponseDTO {
    
    @Schema(description = "ID único del usuario", example = "user123")
    private String id;

    @Schema(
        description = "Nombre de usuario único", 
        example = "johndoe",
        maxLength = 50
    )
    private String username;

    @Schema(
        description = "Dirección de correo electrónico", 
        example = "john.doe@example.com",
        format = "email",
        maxLength = 254
    )
    private String email;

    @Schema(
        description = "Puntaje actual del usuario en el juego", 
        example = "1500",
        minimum = "0"
    )
    private int score;

    @Schema(
        description = "Nivel actual del usuario en el juego", 
        example = "5",
        minimum = "1"
    )
    private int level;

    @Schema(
        description = "Biografía del usuario", 
        example = "Cazador experto de laberintos",
        maxLength = 200
    )
    private String bio;

    @Schema(
        description = "Color del avatar en formato hexadecimal", 
        example = "#A46AFF"
    )
    private String avatarColor;

    @Schema(
        description = "Tamaño preferido de laberinto", 
        example = "Mediano"
    )
    private String preferredMazeSize;
}
