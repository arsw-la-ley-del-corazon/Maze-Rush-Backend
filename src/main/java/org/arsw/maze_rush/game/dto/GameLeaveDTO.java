package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eventos de salida de jugadores del juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameLeaveDTO {
    private String username;
    private String lobbyCode;
}
