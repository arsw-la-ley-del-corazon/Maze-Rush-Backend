package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eventos de unión de jugadores al juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameJoinDTO {
    private String username;
    private String lobbyCode;
}
