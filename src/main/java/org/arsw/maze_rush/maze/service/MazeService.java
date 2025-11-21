package org.arsw.maze_rush.maze.service;

import org.arsw.maze_rush.maze.entities.MazeEntity;

public interface MazeService {

    MazeEntity generateMaze(String size);
    MazeEntity getMazeById(java.util.UUID id);
}
