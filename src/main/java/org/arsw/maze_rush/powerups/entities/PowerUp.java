package org.arsw.maze_rush.powerups.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PowerUp {
    private PowerUpType type;
    private int duration;
    private int x;
    private int y;
}
