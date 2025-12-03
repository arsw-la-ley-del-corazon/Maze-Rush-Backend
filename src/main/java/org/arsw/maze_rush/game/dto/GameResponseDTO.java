package org.arsw.maze_rush.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.maze.dto.MazeResponseDTO;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Data
@Schema(
    name = "GameResponse",
    description = "Respuesta detallada con la información de una partida del juego Maze Rush."
)
public class GameResponseDTO {

    @Schema(description = "ID único del juego (UUID generado automáticamente).", example = "b6f1f1c2-4b32-4a4e-b3c3-1c53e5b8477d")
    private UUID id;

    @Schema(description = "Código del lobby asociado a este juego.", example = "ABC123")
    private String lobbyCode;

    @Schema(description = "Estado actual del juego (EN_CURSO, FINALIZADO, etc.).", example = "EN_CURSO")
    private String status;

    @Schema(description = "Fecha y hora de inicio del juego (ISO 8601).", example = "2025-10-28T17:24:00Z")
    private LocalDateTime startedAt;

    @Schema(description = "Fecha y hora de finalización del juego, si aplica.", example = "2025-10-28T17:59:00Z")
    private LocalDateTime finishedAt;

    @Schema(description = "Lista de jugadores que participan en el juego.")
    private List<String> players;

    @Schema(description = "Laberinto asociado a la partida.")
    private MazeResponseDTO maze;


    public static GameResponseDTO fromEntity(GameEntity game) {
        GameResponseDTO dto = new GameResponseDTO();

        dto.setId(game.getId());
        dto.setStatus(game.getStatus());
        dto.setStartedAt(game.getStartedAt());
        dto.setFinishedAt(game.getFinishedAt());

        LobbyEntity lobby = game.getLobby();
        if (lobby != null) {
            dto.setLobbyCode(lobby.getCode());
        }

        dto.setPlayers(
                game.getPlayers().stream()
                        .map(UserEntity::getUsername)
                        .collect(Collectors.toList())
        );
        dto.setMaze(MazeResponseDTO.fromEntity(game.getMaze()));
        return dto;
    }
}
