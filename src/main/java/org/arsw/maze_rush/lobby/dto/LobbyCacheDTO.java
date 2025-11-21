package org.arsw.maze_rush.lobby.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class LobbyCacheDTO implements Serializable {

    private UUID id;
    private String code;
    private String mazeSize;
    private int maxPlayers;
    private boolean isPublic;
    private String status;
    private String creatorUsername;
    private List<String> players;  
    public List<String> getPlayers() { return players; }      
    public void setPlayers(List<String> players) { this.players = players; }  
}
