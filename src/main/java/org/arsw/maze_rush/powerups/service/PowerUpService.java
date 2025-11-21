package org.arsw.maze_rush.powerups.service;

import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.maze.entities.MazeEntity;

import java.util.List;
public interface PowerUpService {
    List<PowerUp> generatePowerUps(MazeEntity maze);
    List<PowerUp> generatePowerUps(MazeEntity maze, List<PlayerPosition> players);

}
