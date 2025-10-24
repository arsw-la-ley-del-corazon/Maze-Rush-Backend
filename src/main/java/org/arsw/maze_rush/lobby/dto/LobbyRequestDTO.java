package org.arsw.maze_rush.lobby.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
@Schema(name = "LobbyRequest", description = "Datos de entrada para crear un lobby de juego")
public class LobbyRequestDTO {

    @Schema(
        description = "Tamaño del laberinto (Pequeño, Mediano o Grande)",
        example = "Mediano",
        maxLength = 20
    )
    @NotBlank
    @Size(max = 20)
    private String mazeSize;

    @Schema(
        description = "Número máximo de jugadores permitidos en el lobby (entre 2 y 4)",
        example = "4",
        minimum = "2",
        maximum = "4"
    )
    @Min(2)
    @Max(4)
    private int maxPlayers;

    @Schema(
        description = "Visibilidad del lobby (Pública o Privada)",
        example = "Pública",
        maxLength = 20
    )
    @NotBlank
    @Size(max = 20)
    private String visibility;

    @Schema(
        description = "Nombre de usuario del creador del lobby",
        example = "jugador1",
        maxLength = 50
    )
    @NotBlank
    @Size(max = 50)
    private String creatorUsername;
}
