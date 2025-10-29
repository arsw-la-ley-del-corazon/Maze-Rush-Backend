package org.arsw.maze_rush.game.service;

import org.arsw.maze_rush.game.entities.GameEntity;
import java.util.UUID;

public interface GameService {

    GameEntity startGame(String lobbyCode);
    GameEntity getGameById(UUID id);
    GameEntity finishGame(UUID id); 
}
