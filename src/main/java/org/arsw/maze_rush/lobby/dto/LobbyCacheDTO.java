package org.arsw.maze_rush.lobby.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
public class LobbyCacheDTO implements Serializable {
    private UUID id;
    private String code;
    private String mazeSize;
    private int maxPlayers;
    private boolean isPublic;
    private String status;
    private String creatorUsername;
}
