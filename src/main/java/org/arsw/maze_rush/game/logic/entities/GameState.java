package org.arsw.maze_rush.game.logic.entities;

import lombok.Data;
import java.util.List;
import java.util.UUID;

import org.arsw.maze_rush.powerups.entities.PowerUp;

@Data
public class GameState {
    private UUID gameId;
    private String status;
    private List<PlayerPosition> playerPositions;
    private List<PowerUp> powerUps;
    private String currentLayout;
    

}
