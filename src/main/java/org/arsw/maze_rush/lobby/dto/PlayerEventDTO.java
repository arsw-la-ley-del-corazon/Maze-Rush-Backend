package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para eventos de jugadores (unirse/salir) y actualizaciones de lista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEventDTO {
    private String username;
    private String action; // "joined", "left"
    private List<String> players; // Lista actualizada de jugadores
    private int playerCount;
    private int maxPlayers;
}

