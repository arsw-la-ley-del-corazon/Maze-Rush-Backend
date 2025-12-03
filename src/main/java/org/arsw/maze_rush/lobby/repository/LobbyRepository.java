package org.arsw.maze_rush.lobby.repository;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LobbyRepository extends JpaRepository<LobbyEntity, UUID> {
    Optional<LobbyEntity> findByCode(String code);
}
