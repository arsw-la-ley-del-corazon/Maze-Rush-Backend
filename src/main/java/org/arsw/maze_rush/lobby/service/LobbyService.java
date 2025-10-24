package org.arsw.maze_rush.lobby.service;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;

import java.util.List;
import java.util.UUID;

public interface LobbyService {

    LobbyEntity createLobby(String mazeSize, int maxPlayers, boolean isPublic, String status, String creatorUsername);
    List<LobbyEntity> getAllLobbies();
    LobbyEntity getLobbyByCode(String code);
    void deleteLobby(UUID id);
    void addPlayerToLobby(UUID lobbyId, UUID userId);
    void removePlayerFromLobby(UUID lobbyId, UUID userId);

}
