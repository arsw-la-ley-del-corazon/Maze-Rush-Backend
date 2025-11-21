package org.arsw.maze_rush.maze.repository;

import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MazeRepository extends JpaRepository<MazeEntity, UUID> {
}
