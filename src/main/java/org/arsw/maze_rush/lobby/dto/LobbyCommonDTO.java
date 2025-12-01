package org.arsw.maze_rush.lobby.dto;

public interface LobbyCommonDTO {

    void setId(String id);
    void setCode(String code);
    void setMazeSize(String mazeSize);
    void setMaxPlayers(int maxPlayers);
    void setPublic(boolean isPublic);
    void setStatus(String status);
    void setCreatorUsername(String username);
    void setCreatedAt(String createdAt);
}
