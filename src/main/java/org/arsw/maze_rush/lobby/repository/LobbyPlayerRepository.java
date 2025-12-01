package org.arsw.maze_rush.lobby.repository;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.entities.LobbyPlayerEntity;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LobbyPlayerRepository extends JpaRepository<LobbyPlayerEntity, UUID> {
    
    /**
     * Busca todos los jugadores de un lobby específico
     */
    List<LobbyPlayerEntity> findByLobby(LobbyEntity lobby);
    
    /**
     * Busca un jugador específico en un lobby
     */
    Optional<LobbyPlayerEntity> findByLobbyAndUser(LobbyEntity lobby, UserEntity user);
    
    /**
     * Verifica si un usuario está en un lobby
     */
    boolean existsByLobbyAndUser(LobbyEntity lobby, UserEntity user);
    
    /**
     * Elimina a un jugador de un lobby
     */
    void deleteByLobbyAndUser(LobbyEntity lobby, UserEntity user);
}
