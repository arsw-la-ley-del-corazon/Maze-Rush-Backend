package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO para sincronización completa del estado del juego
 * Se envía periódicamente a todos los jugadores
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSyncDTO {
    private String type = "sync";
    private List<PlayerGameStateDTO> players;
    private Instant timestamp;
    
    public GameSyncDTO(List<PlayerGameStateDTO> players) {
        this.players = players;
        this.timestamp = Instant.now();
    }
}
