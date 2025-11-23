package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para eventos cuando un jugador termina el laberinto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFinishDTO {
    private String username;
    private Long finishTime; // Tiempo en segundos
    private Instant timestamp;
    
    public GameFinishDTO(String username, Long finishTime) {
        this.username = username;
        this.finishTime = finishTime;
        this.timestamp = Instant.now();
    }
}
