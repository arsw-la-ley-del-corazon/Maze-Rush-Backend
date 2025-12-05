package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para eventos de movimiento de jugadores
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMoveDTO {
    private String type = "move";
    private String username;
    private PositionDTO position;
    private Instant timestamp;
    
    public GameMoveDTO(String username, PositionDTO position) {
        this.type = "move";
        this.username = username;
        this.position = position;
        this.timestamp = Instant.now();
    }
}
