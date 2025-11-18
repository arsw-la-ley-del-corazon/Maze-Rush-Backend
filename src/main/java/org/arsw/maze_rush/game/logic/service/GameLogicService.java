package org.arsw.maze_rush.game.logic.service;

import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;

import java.util.UUID;

public interface GameLogicService {

    GameState initializeGame(UUID gameId);

    GameState movePlayer(UUID gameId, PlayerMoveRequestDTO moveRequest);

    GameState getCurrentState(UUID gameId);
}
