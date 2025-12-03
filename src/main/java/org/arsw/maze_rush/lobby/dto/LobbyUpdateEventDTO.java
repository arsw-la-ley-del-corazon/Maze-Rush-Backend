package org.arsw.maze_rush.lobby.dto;

import lombok.Data;

/**
 * DTO para eventos de actualización de lobbies públicos
 * Se envía a través de WebSocket a /topic/lobby/updates
 */
@Data
public class LobbyUpdateEventDTO {
    private String code;
    private int playerCount;
    private String status;
    private String action; // "created", "updated", "deleted"
}
