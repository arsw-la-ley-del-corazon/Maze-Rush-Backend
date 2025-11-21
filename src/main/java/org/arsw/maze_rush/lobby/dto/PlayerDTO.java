package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un jugador en un lobby
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private String id;
    private String username;
    private Boolean isReady;
    private Boolean isHost;
    private Integer level;
    private Integer score;
}
