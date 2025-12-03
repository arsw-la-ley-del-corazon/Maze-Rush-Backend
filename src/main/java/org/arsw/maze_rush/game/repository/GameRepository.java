package org.arsw.maze_rush.game.repository;

import org.arsw.maze_rush.game.entities.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
    Optional<GameEntity> findByLobby_Code(String code);
}
