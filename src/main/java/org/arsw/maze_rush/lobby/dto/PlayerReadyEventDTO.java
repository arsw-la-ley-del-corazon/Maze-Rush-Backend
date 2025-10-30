package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eventos de estado de jugador (listo/no listo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerReadyEventDTO {
    private String playerId;
    private String username;
    private Boolean isReady;
}
