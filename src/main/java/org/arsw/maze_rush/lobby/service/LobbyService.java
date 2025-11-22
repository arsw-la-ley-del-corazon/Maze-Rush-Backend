package org.arsw.maze_rush.lobby.service;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;

import java.util.List;
import java.util.UUID;

public interface LobbyService {

    LobbyEntity createLobby(String mazeSize, int maxPlayers, boolean isPublic, String status, String creatorUsername);
    List<LobbyEntity> getAllLobbies();
    LobbyEntity getLobbyByCode(String code);
    void removePlayerFromLobby(UUID lobbyId, UUID userId);
    LobbyEntity joinLobbyByCode(String code, String username);
    void leaveLobby(String code, String username);
    
    // WebSocket methods
    void sendChatMessage(String code, String username, String message);
    void toggleReady(String code, String username);
    void startGame(String code, String username);

}
