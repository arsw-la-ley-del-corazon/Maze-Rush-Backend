package org.arsw.maze_rush.lobby.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    name = "LobbyRequest",
    description = "Datos de entrada requeridos para crear un lobby de juego."
)
public class LobbyRequestDTO {

    @Schema(
        description = "Tamaño del laberinto (Pequeño, Mediano o Grande).",
        example = "Mediano",
        maxLength = 20
    )
    @NotBlank(message = "El tamaño del laberinto no puede estar vacío.")
    @Size(max = 20, message = "El tamaño del laberinto no puede superar los 20 caracteres.")
    private String mazeSize;

    @Schema(
        description = "Número máximo de jugadores permitidos en el lobby (entre 2 y 4).",
        example = "4",
        minimum = "2",
        maximum = "4"
    )
    @Min(value = 2, message = "Debe haber al menos 2 jugadores.")
    @Max(value = 4, message = "El número máximo de jugadores es 4.")
    private int maxPlayers;

    @Schema(
        description = "Define si el lobby será público o privado.",
        example = "true"
    )
    @JsonProperty("isPublic")
    private boolean isPublic = true;  // Por defecto público

    @Schema(
        description = "Estado inicial del lobby (opcional, se define automáticamente si no se envía).",
        example = "EN_ESPERA",
        maxLength = 20
    )
    @Size(max = 20, message = "El estado no puede superar los 20 caracteres.")
    private String status = "EN_ESPERA";

}
