package org.arsw.maze_rush.game.logic.entities;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class GameState {
    private UUID gameId;
    private String status;
    private List<PlayerPosition> playerPositions;
}
