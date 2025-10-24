package org.arsw.maze_rush.lobby.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LobbyResponse", description = "Respuesta con la información del lobby creado")
public class LobbyResponseDTO {

    @Schema(description = "ID único del lobby", example = "6f7b10b5-5df4-4b61-9e48-92b50e87f3c2")
    private String id;

    @Schema(description = "Código único de 6 caracteres del lobby", example = "ABC123")
    private String code;

    @Schema(description = "Tamaño del laberinto seleccionado", example = "Mediano")
    private String mazeSize;

    @Schema(description = "Número máximo de jugadores permitidos", example = "4")
    private int maxPlayers;

    @Schema(description = "Visibilidad del lobby (Pública o Privada)", example = "Pública")
    private String visibility;

    @Schema(description = "Nombre del creador del lobby", example = "jugador1")
    private String creatorUsername;
}
