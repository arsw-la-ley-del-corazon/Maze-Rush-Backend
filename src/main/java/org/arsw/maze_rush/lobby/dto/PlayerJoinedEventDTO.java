package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para evento cuando un jugador se une o sale del lobby
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerJoinedEventDTO {
    private PlayerDTO player;
    private String action; // "joined" o "left"
}
