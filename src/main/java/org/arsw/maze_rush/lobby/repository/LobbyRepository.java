package org.arsw.maze_rush.lobby.repository;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.entities.LobbyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LobbyRepository extends JpaRepository<LobbyEntity, UUID> {
    
    /**
     * Busca un lobby por su código único
     */
    Optional<LobbyEntity> findByCode(String code);
    
    /**
     * Verifica si existe un lobby con el código dado
     */
    boolean existsByCode(String code);
    
    /**
     * Busca todos los lobbies con un estado específico
     */
    java.util.List<LobbyEntity> findByStatus(LobbyStatus status);
}
