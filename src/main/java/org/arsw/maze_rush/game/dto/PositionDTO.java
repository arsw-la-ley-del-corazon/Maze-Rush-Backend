package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una posición en el laberinto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {
    private int x;
    private int y;
}
