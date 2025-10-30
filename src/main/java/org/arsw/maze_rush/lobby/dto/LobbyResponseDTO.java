package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta al crear o unirse a un lobby
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyResponseDTO {
    private String lobbyId;
    private String code;
    private Integer maxPlayers;
    private Boolean isPrivate;
    private String status;
    private PlayerDTO host;
    private java.util.List<PlayerDTO> players;
}
