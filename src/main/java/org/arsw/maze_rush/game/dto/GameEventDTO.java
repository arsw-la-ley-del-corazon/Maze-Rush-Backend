package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para eventos genéricos del juego (player_joined, player_left)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEventDTO {
    private String type; // "player_joined" | "player_left" | "start"
    private String username;
    private Instant timestamp;
    
    public GameEventDTO(String type, String username) {
        this.type = type;
        this.username = username;
        this.timestamp = Instant.now();
    }
}

