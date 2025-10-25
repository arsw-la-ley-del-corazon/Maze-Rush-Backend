package org.arsw.maze_rush.lobby.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(
    name = "LobbyWithPlayersResponse",
    description = "Respuesta con la información del lobby y los jugadores asociados."
)
public class LobbyWithPlayersResponseDTO {

    @Schema(description = "ID único del lobby (UUID generado automáticamente).")
    private String id;

    @Schema(description = "Código único de 6 caracteres asignado al lobby.")
    private String code;

    @Schema(description = "Tamaño del laberinto seleccionado para el juego.")
    private String mazeSize;

    @Schema(description = "Número máximo de jugadores permitidos en el lobby.")
    private int maxPlayers;

    @Schema(description = "Indica si el lobby es público o privado.")
    private boolean isPublic;

    @Schema(description = "Estado actual del lobby (En espera, En juego, Finalizado).")
    private String status;

    @Schema(description = "Nombre del usuario que creó el lobby.")
    private String creatorUsername;

    @Schema(description = "Fecha y hora de creación del lobby (ISO 8601).")
    private String createdAt;

    @Schema(description = "Lista de nombres de usuario de los jugadores que están en el lobby.")
    private List<String> players;
}
