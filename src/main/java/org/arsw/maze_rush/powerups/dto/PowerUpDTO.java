package org.arsw.maze_rush.powerups.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PowerUpDTO {
    private String type;
    private int x;
    private int y;
    private int duration;
}
