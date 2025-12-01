package org.arsw.maze_rush.lobby.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(
    name = "LobbyResponse",
    description = "Respuesta con la información detallada de un lobby en el sistema."
)
public class LobbyResponseDTO implements LobbyCommonDTO{

    @Schema(
        description = "ID único del lobby (UUID generado automáticamente).",
        example = "6f7b10b5-5df4-4b61-9e48-92b50e87f3c2"
    )
    private String id;

    @Schema(
        description = "Código único de 6 caracteres asignado al lobby.",
        example = "ABC123"
    )
    private String code;

    @Schema(
        description = "Tamaño del laberinto seleccionado para el juego.",
        example = "Mediano"
    )
    private String mazeSize;

    @Schema(
        description = "Número máximo de jugadores permitidos en el lobby (entre 2 y 4).",
        example = "4"
    )
    private int maxPlayers;

    @Schema(
        description = "Número actual de jugadores conectados en el lobby.",
        example = "2"
    )
    private int currentPlayers;

    @Schema(
        description = "Indica si el lobby es público (true) o privado (false).",
        example = "true"
    )
    @JsonProperty("isPublic")
    private boolean isPublic;

    @Schema(
        description = "Estado actual del lobby (por ejemplo: 'En espera', 'En juego', 'Finalizado').",
        example = "En espera"
    )
    private String status;

    @Schema(
        description = "Nombre del usuario que creó el lobby.",
        example = "jugador1"
    )
    private String creatorUsername;

    @Schema(
        description = "Fecha y hora en la que se creó el lobby (en formato ISO-8601).",
        example = "2025-10-24T02:14:13Z"
    )
    private String createdAt;
}
